package com.example.urlShortner.exception;

public class ReservedAliasException extends RuntimeException {

	private final String alias;

	public ReservedAliasException(String alias) {
		super("Alias is reserved and cannot be used: " + alias);
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}
}
