package com.example.urlShortner.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ShortUrlPathTest {

	@Test
	void parse_directPath() {
		assertThat(ShortUrlPath.parse("001fXm2").getShortCode()).isEqualTo("001fXm2");
		assertThat(ShortUrlPath.parse("/001fXm2/").getShortCode()).isEqualTo("001fXm2");
	}

	@Test
	void parse_brandedPath() {
		assertThat(ShortUrlPath.parse("akash/001fXm2").getShortCode()).isEqualTo("001fXm2");
		assertThat(ShortUrlPath.parse("/akash/001fXm2/").getShortCode()).isEqualTo("001fXm2");
	}

	@Test
	void parse_rejectsInvalidDepth() {
		assertThatThrownBy(() -> ShortUrlPath.parse("a/b/c"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("depth");
	}

	@Test
	void parse_rejectsInvalidCode() {
		assertThatThrownBy(() -> ShortUrlPath.parse("akash/short"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("7 alphanumeric");
	}
}
