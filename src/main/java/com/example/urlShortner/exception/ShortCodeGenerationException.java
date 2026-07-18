package com.example.urlShortner.exception;

public class ShortCodeGenerationException extends RuntimeException {

	public ShortCodeGenerationException(int attempts) {
		super("Failed to generate a unique short code after " + attempts + " attempts");
	}
}
