package com.example.urlShortner.exception;

public class AliasAlreadyExistsException extends RuntimeException {

	private final String alias;

	public AliasAlreadyExistsException(String alias) {
		super("Alias already in use: " + alias);
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}
}
