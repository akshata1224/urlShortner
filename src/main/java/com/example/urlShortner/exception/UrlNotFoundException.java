package com.example.urlShortner.exception;

public class UrlNotFoundException extends RuntimeException {

	private final String code;

	public UrlNotFoundException(String code) {
		super("No URL found for code: " + code);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
