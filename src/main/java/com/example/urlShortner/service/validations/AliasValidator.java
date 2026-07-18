package com.example.urlShortner.service.validations;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.urlShortner.exception.InvalidAliasException;
import com.example.urlShortner.exception.ReservedAliasException;

/**
 * Validates short-URL aliases / codes against application rules.
 * <p>
 * Call {@link #validateBeforeCreate(String)} <strong>before</strong> creating or
 * persisting a short URL, so invalid aliases are rejected early.
 */
@Component
public class AliasValidator {

	private static final int MIN_LENGTH = 3;
	private static final int MAX_LENGTH = 32;

	/**
	 * Must start with a letter or digit; may then contain letters, digits, {@code _} or {@code -}.
	 */
	private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]*$");

	/**
	 * Aliases that must never be claimed by users or auto-generation.
	 * Compared case-insensitively so they cannot clash with HTTP routes
	 * (for example {@code /api}, {@code /actuator}).
	 */
	private static final Set<String> RESERVED_ALIASES = Set.of(
			"api",
			"actuator",
			"error",
			"favicon.ico",
			"swagger",
			"swagger-ui",
			"v1",
			"health"
	);

	/**
	 * Full pre-create check for a custom alias.
	 * <p>
	 * Runs format + reserved-name checks. Must be invoked before building
	 * or storing a {@code ShortUrlEntry}.
	 *
	 * @param alias candidate alias from the client (already trimmed)
	 * @throws InvalidAliasException if format/length is invalid
	 * @throws ReservedAliasException if the alias is a reserved system name
	 */
	public void validateBeforeCreate(String alias) {
		validateFormat(alias);
		validateNotReserved(alias);
	}

	/**
	 * Ensures the alias has a valid length and character pattern.
	 */
	public void validateFormat(String alias) {
		if (alias == null || alias.isBlank()) {
			throw new InvalidAliasException(alias, "Alias is required");
		}
		if (alias.length() < MIN_LENGTH || alias.length() > MAX_LENGTH) {
			throw new InvalidAliasException(
					alias,
					"Custom alias must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"
			);
		}
		if (!ALIAS_PATTERN.matcher(alias).matches()) {
			throw new InvalidAliasException(
					alias,
					"Custom alias may contain letters, digits, underscore and hyphen, "
							+ "and must start with an alphanumeric character"
			);
		}
	}

	/**
	 * Ensures a custom alias is not a reserved system path.
	 *
	 * @param alias candidate alias from the client
	 * @throws ReservedAliasException if the alias is reserved
	 */
	public void validateNotReserved(String alias) {
		if (isReserved(alias)) {
			throw new ReservedAliasException(alias);
		}
	}

	/**
	 * Returns {@code true} when the given value matches a reserved name.
	 * Used both for rejecting custom aliases and for skipping reserved
	 * codes during auto-generation.
	 *
	 * @param alias alias or generated short code
	 * @return whether the value is reserved
	 */
	public boolean isReserved(String alias) {
		if (alias == null || alias.isBlank()) {
			return false;
		}
		return RESERVED_ALIASES.contains(alias.toLowerCase(Locale.ROOT));
	}
}
