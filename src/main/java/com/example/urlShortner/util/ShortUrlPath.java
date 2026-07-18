package com.example.urlShortner.util;

import java.util.regex.Pattern;

/**
 * Immutable domain abstraction that cleanly parses and validates
 * short URL routing paths.
 */
public final class ShortUrlPath {

	private static final Pattern VALID_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{7}$");

	private final String shortCode;

	private ShortUrlPath(String shortCode) {
		this.shortCode = shortCode;
	}

	/**
	 * Factory method that parses the incoming URL path and extracts the short code.
	 * Throws an IllegalArgumentException if the path is malformed.
	 */
	public static ShortUrlPath parse(String rawPath) {
		if (rawPath == null || rawPath.isBlank()) {
			throw new IllegalArgumentException("Routing path cannot be blank.");
		}

		// Clean up leading/trailing slashes (e.g., "/akash/001fXm2/" -> "akash/001fXm2")
		String cleaned = rawPath.replaceAll("^/+", "").replaceAll("/+$", "");
		String[] segments = cleaned.split("/");

		// Determine code based on path depth hierarchy
		String code = switch (segments.length) {
			case 1 -> segments[0]; // Option B: Direct Path (bit.ly/001fXm2)
			case 2 -> segments[1]; // Option A: Branded Path (bit.ly/akash/001fXm2)
			default -> throw new IllegalArgumentException("Invalid URL path depth.");
		};

		// Strict validation: must be exactly 7 alphanumeric characters
		if (!VALID_CODE_PATTERN.matcher(code).matches()) {
			throw new IllegalArgumentException("Short code must be exactly 7 alphanumeric characters.");
		}

		return new ShortUrlPath(code);
	}

	public String getShortCode() {
		return shortCode;
	}
}
