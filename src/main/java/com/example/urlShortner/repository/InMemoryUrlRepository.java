package com.example.urlShortner.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.example.urlShortner.model.ShortUrlEntry;

/**
 * Flat high-speed storage keyed by the universally unique 7-character short code.
 * Custom alias is only a URL prefix — not part of the storage key.
 */
@Repository
public class InMemoryUrlRepository {

	private final Map<String, ShortUrlEntry> storage = new ConcurrentHashMap<>();

	public Optional<ShortUrlEntry> findByShortCode(String shortCode) {
		return Optional.ofNullable(storage.get(shortCode));
	}

	/**
	 * Atomically inserts a short code entry if it does not already exist.
	 * 
	 * @return An Optional containing the conflicting entry if a duplicate exists,
	 *         or Optional.empty() if the write was successful.
	 */
	public Optional<ShortUrlEntry> save(String shortCode, ShortUrlEntry entry) {
		// putIfAbsent returns the existing value if the key was already there, or null if it was fresh
		ShortUrlEntry existingValue = storage.putIfAbsent(shortCode, entry);
		return Optional.ofNullable(existingValue);
	}

	public void invalidate(String shortCode) {
		storage.remove(shortCode);
	}

	public int size() {
		return storage.size();
	}

	/** Test helper. */
	public void clear() {
		storage.clear();
	}
}
