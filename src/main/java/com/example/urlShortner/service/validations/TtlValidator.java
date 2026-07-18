package com.example.urlShortner.service.validations;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.example.urlShortner.config.AppProperties;
import com.example.urlShortner.exception.InvalidTtlException;

/**
 * Resolves and validates TTL (time-to-live) for newly created short URLs.
 * <p>
 * When the client omits {@code ttlSeconds}, {@link AppProperties#defaultTtl()} is used.
 * Values above {@link AppProperties#maxTtl()} are rejected.
 */
@Component
public class TtlValidator {

	private final AppProperties appProperties;

	public TtlValidator(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	/**
	 * Resolves the effective TTL for a create request.
	 *
	 * @param ttlSeconds optional TTL from the client (seconds); {@code null} means default
	 * @return a positive duration not exceeding the configured maximum
	 * @throws InvalidTtlException if the TTL is zero, negative, or above the max
	 */
	public Duration resolveTtl(Integer ttlSeconds) {
		Duration ttl = ttlSeconds == null
				? appProperties.defaultTtl()
				: Duration.ofSeconds(ttlSeconds);

		if (ttl.isZero() || ttl.isNegative()) {
			throw new InvalidTtlException("TTL must be a positive duration");
		}

		if (ttl.compareTo(appProperties.maxTtl()) > 0) {
			throw new InvalidTtlException(
					"TTL exceeds maximum allowed of " + appProperties.maxTtl().toSeconds() + " seconds"
			);
		}

		return ttl;
	}
}
