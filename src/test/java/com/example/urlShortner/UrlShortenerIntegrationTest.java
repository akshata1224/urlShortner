package com.example.urlShortner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.urlShortner.dto.CreateShortUrlRequest;
import com.example.urlShortner.dto.ShortUrlResponse;
import com.example.urlShortner.exception.UrlExpiredException;
import com.example.urlShortner.repository.InMemoryUrlRepository;
import com.example.urlShortner.service.UrlShortenerService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UrlShortenerService urlShortenerService;

	@Autowired
	private InMemoryUrlRepository repository;

	@BeforeEach
	void setUp() {
		repository.clear();
	}

	@Test
	void post_createWithoutAlias() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "url": "https://example.com/docs/guide"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.shortCode", hasLength(7)))
				.andExpect(jsonPath("$.customAlias").value(nullValue()))
				.andExpect(jsonPath("$.originalUrl").value("https://example.com/docs/guide"))
				.andExpect(jsonPath("$.shortUrl").isNotEmpty())
				.andExpect(jsonPath("$.accessCount").value(0));
	}

	@Test
	void post_createWithCustomAliasInPayload() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "url": "https://example.com/landing",
								  "customAlias": "Akash",
								  "ttlSeconds": 3600
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.customAlias").value("akash"))
				.andExpect(jsonPath("$.shortCode", hasLength(7)))
				.andReturn();

		String shortCode = JsonPath.read(created.getResponse().getContentAsString(), "$.shortCode");
		String shortUrl = JsonPath.read(created.getResponse().getContentAsString(), "$.shortUrl");

		assertThat(shortUrl).isEqualTo("http://localhost:8080/akash/" + shortCode);
	}

	@Test
	void get_redirectDirectPath() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "url": "https://example.com/direct"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		String shortCode = JsonPath.read(created.getResponse().getContentAsString(), "$.shortCode");

		mockMvc.perform(get("/" + shortCode))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://example.com/direct"))
				.andExpect(header().string("Cache-Control", "no-cache"));
	}

	@Test
	void get_redirectBrandedPath_extractsShortCode() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "url": "https://example.com/auto",
								  "customAlias": "akash"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		String shortCode = JsonPath.read(created.getResponse().getContentAsString(), "$.shortCode");

		mockMvc.perform(get("/akash/" + shortCode))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://example.com/auto"));
	}

	@Test
	void get_invalidPathDepth_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/a/b/c"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid Path"));
	}

	@Test
	void resolveIncrementsAccessCount() {
		ShortUrlResponse created = urlShortenerService.createUrl(
				new CreateShortUrlRequest("https://example.com/hot", "hot", null));

		urlShortenerService.resolveUrl(created.shortCode());
		urlShortenerService.resolveUrl(created.shortCode());

		assertThat(urlShortenerService.getMetadata(created.shortCode()).accessCount()).isEqualTo(2);
	}

	@Test
	void expiredUrl_redirectReturnsGone() throws Exception {
		ShortUrlResponse created = urlShortenerService.createUrl(
				new CreateShortUrlRequest("https://example.com/temp", null, 1));

		TimeUnit.MILLISECONDS.sleep(1100);

		mockMvc.perform(get("/" + created.shortCode()))
				.andExpect(status().isGone())
				.andExpect(jsonPath("$.title").value("URL Expired"));

		assertThatThrownBy(() -> urlShortenerService.resolveUrl(created.shortCode()))
				.isInstanceOfAny(UrlExpiredException.class,
						com.example.urlShortner.exception.UrlNotFoundException.class);
	}

	@Test
	void ttlAboveMax_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "url": "https://example.com/x",
								  "ttlSeconds": 99999999
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void reservedAlias_isRejected() {
		assertThatThrownBy(() -> urlShortenerService.createUrl(
				new CreateShortUrlRequest("https://example.com", "api", null)))
				.isInstanceOf(com.example.urlShortner.exception.ReservedAliasException.class);
	}

	@Test
	void unknownCode_returnsNotFound() throws Exception {
		mockMvc.perform(get("/zzzzzzz"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("URL Not Found"));
	}
}
