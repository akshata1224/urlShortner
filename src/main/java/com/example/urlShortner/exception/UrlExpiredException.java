package com.example.urlShortner.exception;

import java.time.Instant;

public class UrlExpiredException extends RuntimeException {

	private final String code;
	private final Instant expiresAt;

	public UrlExpiredException(String code, Instant expiresAt) {
		super("Short URL has expired: " + code);
		this.code = code;
		this.expiresAt = expiresAt;
	}

	public String getCode() {
		return code;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
