package com.example.urlShortner.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateShortUrlRequest(
		@NotBlank(message = "URL is required")
		@Size(max = 2048, message = "URL must be at most 2048 characters")
		@Pattern(
				regexp = "^(https?://).+",
				message = "URL must start with http:// or https://"
		)
		String url,

		@Size(min = 3, max = 32, message = "Custom alias must be between 3 and 32 characters")
		@Pattern(
				regexp = "^[a-zA-Z0-9][a-zA-Z0-9_-]*$",
				message = "Custom alias may contain letters, digits, underscore and hyphen, and must start with an alphanumeric character"
		)
		String customAlias,

		/**
		 * Optional TTL in seconds. When omitted, {@code app.default-ttl} is used.
		 */
		@Min(value = 1, message = "TTL must be at least 1 second")
		@Max(value = 31_536_000, message = "TTL must be at most 1 year (31536000 seconds)")
		Integer ttlSeconds
) {
}
