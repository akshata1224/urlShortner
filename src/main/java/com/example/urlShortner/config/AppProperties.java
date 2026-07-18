package com.example.urlShortner.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
		@NotBlank String baseUrl,
		@Min(4) @Max(16) int shortCodeLength,
		@Min(1) @Max(64) int maxGenerationAttempts,
		@NotNull Duration defaultTtl,
		@NotNull Duration maxTtl,
		@Positive long cacheMaxSize
) {
}
