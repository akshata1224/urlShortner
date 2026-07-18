package com.example.urlShortner.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.urlShortner.dto.CreateShortUrlRequest;
import com.example.urlShortner.dto.ShortUrlResponse;
import com.example.urlShortner.service.UrlShortenerService;

import jakarta.validation.Valid;

/**
 * Production-Ready Single Unified URL Shortener Router Engine.
 * Implements the absolute minimal 2-API surface contract.
 */
@Validated
@RestController // ◄ NOTICE: No @RequestMapping here anymore! This opens the class to the root.
public class UrlShortenerController {

	private static final Logger log = LoggerFactory.getLogger(UrlShortenerController.class);

	private final UrlShortenerService urlService;

	public UrlShortenerController(UrlShortenerService urlService) {
		this.urlService = urlService;
	}

	/**
	 * UNIFIED POST API: Creates a shortened link.
	 * Explicitly declares its administrative path here.
	 */
	@PostMapping("/api/v1/urls") // ◄ FIXED: Path moved from class to here
	public ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
		log.info("Received request to shorten URL. HasCustomAlias={}", request.customAlias() != null);

		ShortUrlResponse response = urlService.createUrl(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * UNIFIED GET API: Catches all traffic hitting the root domain.
	 * Passes the raw remaining segments directly to the service parsing layout.
	 */
	@GetMapping("/{*remainingPath}") // ◄ FIXED: This now successfully listens at the absolute root "/"
	public ResponseEntity<Void> handleRedirect(@PathVariable String remainingPath) {
		// Production Safety Guard: Ignore automated file sweeps before parsing segments
		if (remainingPath != null && (remainingPath.contains("favicon.ico") || remainingPath.contains(".json"))) {
			return ResponseEntity.notFound().build();
		}
		
		String originalUrl = urlService.resolveUrl(remainingPath);

		log.debug("Successful redirect: path={} -> target={}", remainingPath, originalUrl);

		return ResponseEntity.status(HttpStatus.FOUND) // HTTP 302 Redirect
				.location(URI.create(originalUrl))
				.cacheControl(CacheControl.noCache()) // Prevent local browser caching to preserve tracking stats
				.build();
	}
}
