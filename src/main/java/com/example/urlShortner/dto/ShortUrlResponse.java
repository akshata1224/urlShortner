package com.example.urlShortner.dto;

import java.time.Instant;

public record ShortUrlResponse(
		String customAlias,
		String shortCode,
		String originalUrl,
		String shortUrl,
		Instant createdAt,
		Instant expiresAt,
		long accessCount
) {
}
