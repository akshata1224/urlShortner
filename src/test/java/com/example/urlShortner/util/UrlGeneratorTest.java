package com.example.urlShortner.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UrlGeneratorTest {

	@Test
	void alphabet_is62Chars_lowerThenUpperThenDigits() {
		assertThat(UrlGenerator.alphabet())
				.isEqualTo("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		assertThat(UrlGenerator.alphabet()).hasSize(62);
		assertThat(UrlGenerator.targetLength()).isEqualTo(7);
	}

	@Test
	void encode_alwaysReturnsExactly7Characters() {
		assertThat(UrlGenerator.encode(0)).isEqualTo("aaaaaaa");
		assertThat(UrlGenerator.encode(1)).isEqualTo("aaaaaab");
		assertThat(UrlGenerator.encode(61)).isEqualTo("aaaaaa9");
		assertThat(UrlGenerator.encode(62)).isEqualTo("aaaaaba");

		String code = UrlGenerator.encode(152637485960L);
		assertThat(code).hasSize(7);
		assertThat(UrlGenerator.decode(code)).isEqualTo(152637485960L);
	}

	@Test
	void encodeDecode_roundTrip() {
		long[] samples = {0L, 1L, 61L, 62L, 125L, 999_999L, 152637485960L};
		for (long id : samples) {
			String code = UrlGenerator.encode(id);
			assertThat(code).hasSize(7);
			assertThat(UrlGenerator.decode(code)).isEqualTo(id);
		}
	}

	@Test
	void encode_rejectsNegativeId() {
		assertThatThrownBy(() -> UrlGenerator.encode(-1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("positive");
	}

	@Test
	void decode_rejectsInvalidCharacter() {
		assertThatThrownBy(() -> UrlGenerator.decode("00000-1"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid character");
	}
}
