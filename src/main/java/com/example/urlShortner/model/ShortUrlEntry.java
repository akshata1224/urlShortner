package com.example.urlShortner.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-memory short-URL record.
 * Primary lookup key is always {@code shortCode} (7-char Base62).
 * {@code customAlias} is an optional URL path prefix only (e.g. /akash/001fXm2).
 */
public final class ShortUrlEntry {

	private final long id;
	private final String customAlias;
	private final String shortCode;
	private final String originalUrl;
	private final Instant createdAt;
	private final Instant expiresAt;
	private final AtomicLong accessCount = new AtomicLong(0);
	private final AtomicReference<Instant> lastAccessedAt = new AtomicReference<>();

	public ShortUrlEntry(
			long id,
			String customAlias,
			String shortCode,
			String originalUrl,
			Instant createdAt,
			Instant expiresAt) {
		this.id = id;
		this.customAlias = customAlias;
		this.shortCode = shortCode;
		this.originalUrl = originalUrl;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}

	public long getId() {
		return id;
	}

	public String getCustomAlias() {
		return customAlias;
	}

	public String getShortCode() {
		return shortCode;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public long getAccessCount() {
		return accessCount.get();
	}

	public Instant getLastAccessedAt() {
		return lastAccessedAt.get();
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public long recordAccess() {
		lastAccessedAt.set(Instant.now());
		return accessCount.incrementAndGet();
	}
}
