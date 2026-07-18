package com.example.urlShortner.exception;

public class InvalidAliasException extends RuntimeException {

	private final String alias;

	public InvalidAliasException(String alias, String message) {
		super(message);
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}
}
