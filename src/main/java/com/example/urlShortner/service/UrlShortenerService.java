package com.example.urlShortner.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.urlShortner.config.AppProperties;
import com.example.urlShortner.dto.CreateShortUrlRequest;
import com.example.urlShortner.dto.ShortUrlResponse;
import com.example.urlShortner.exception.ShortCodeGenerationException;
import com.example.urlShortner.exception.UrlExpiredException;
import com.example.urlShortner.exception.UrlNotFoundException;
import com.example.urlShortner.model.ShortUrlEntry;
import com.example.urlShortner.repository.InMemoryUrlRepository;
import com.example.urlShortner.service.validations.AliasValidator;
import com.example.urlShortner.service.validations.TtlValidator;
import com.example.urlShortner.util.ShortUrlPath;
import com.example.urlShortner.util.UrlGenerator;

@Service
public class UrlShortenerService {

	private final InMemoryUrlRepository repository;
	private final AppProperties appProperties;
	private final AliasValidator aliasValidator;
	private final TtlValidator ttlValidator;

	/**
	 * Offset at 62^6 so Base62 encoding is always exactly 7 characters
	 * for the normal id range.
	 */
	private final AtomicLong uniqueIdGenerator = new AtomicLong(56_800_235_584L);

	public UrlShortenerService(
			InMemoryUrlRepository repository,
			AppProperties appProperties,
			AliasValidator aliasValidator,
			TtlValidator ttlValidator) {
		this.repository = repository;
		this.appProperties = appProperties;
		this.aliasValidator = aliasValidator;
		this.ttlValidator = ttlValidator;
	}

	/**
	 * Unified write path for Option A (branded) and Option B (direct root).
	 * Always generates the same unique 7-character short code; only the returned
	 * absolute URL string changes when a custom alias prefix is present.
	 */
	public ShortUrlResponse createUrl(CreateShortUrlRequest request) {
		Duration ttl = ttlValidator.resolveTtl(request.ttlSeconds());
		Instant createdAt = Instant.now();
		Instant expiresAt = createdAt.plus(ttl);

		String customAlias = null;
		if (StringUtils.hasText(request.customAlias())) {
			customAlias = request.customAlias().trim().toLowerCase();
			aliasValidator.validateBeforeCreate(customAlias);
		}

		GeneratedCode generated = nextUniqueShortCode();
		String absoluteShortUrl = buildAbsoluteShortUrl(customAlias, generated.shortCode());

		ShortUrlEntry entry = new ShortUrlEntry(
				generated.id(),
				customAlias,
				generated.shortCode(),
				request.url(),
				createdAt,
				expiresAt
		);

		repository.save(generated.shortCode(), entry);

		return new ShortUrlResponse(
				entry.getCustomAlias(),
				entry.getShortCode(),
				entry.getOriginalUrl(),
				absoluteShortUrl,
				entry.getCreatedAt(),
				entry.getExpiresAt(),
				entry.getAccessCount()
		);
	}

	/**
	 * Unified read path — parses {@code /{shortCode}} or {@code /{alias}/{shortCode}},
	 * then looks up by the extracted short code only.
	 */
	public String resolveUrl(String remainingPath) {
		ShortUrlPath pathInfo = ShortUrlPath.parse(remainingPath);
		String shortCode = pathInfo.getShortCode();

		ShortUrlEntry entry = repository.findByShortCode(shortCode)
				.orElseThrow(() -> new UrlNotFoundException(shortCode));

		if (entry.isExpired()) {
			repository.invalidate(shortCode);
			throw new UrlExpiredException(shortCode, entry.getExpiresAt());
		}

		entry.recordAccess();
		return entry.getOriginalUrl();
	}

	public ShortUrlResponse getMetadata(String shortCode) {
		ShortUrlEntry entry = repository.findByShortCode(shortCode)
				.orElseThrow(() -> new UrlNotFoundException(shortCode));

		if (entry.isExpired()) {
			repository.invalidate(shortCode);
			throw new UrlExpiredException(shortCode, entry.getExpiresAt());
		}

		return new ShortUrlResponse(
				entry.getCustomAlias(),
				entry.getShortCode(),
				entry.getOriginalUrl(),
				buildAbsoluteShortUrl(entry.getCustomAlias(), entry.getShortCode()),
				entry.getCreatedAt(),
				entry.getExpiresAt(),
				entry.getAccessCount()
		);
	}

	private GeneratedCode nextUniqueShortCode() {
		int maxAttempts = appProperties.maxGenerationAttempts();

		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			long id = uniqueIdGenerator.getAndIncrement();
			String shortCode = UrlGenerator.encode(id);

			if (aliasValidator.isReserved(shortCode)) {
				continue;
			}
			if (repository.findByShortCode(shortCode).isPresent()) {
				continue;
			}
			return new GeneratedCode(id, shortCode);
		}

		throw new ShortCodeGenerationException(maxAttempts);
	}

	private String buildAbsoluteShortUrl(String customAlias, String shortCode) {
		String base = appProperties.baseUrl();
		if (StringUtils.hasText(customAlias)) {
			return base + "/" + customAlias + "/" + shortCode;
		}
		return base + "/" + shortCode;
	}

	private record GeneratedCode(long id, String shortCode) {
	}
}
