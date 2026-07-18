package com.example.urlShortner.util;

/**
 * Converts a unique numeric ID into a fixed 7-character Base62 short code (and back).
 * Guaranteed zero collisions when input IDs are unique.
 */
public final class UrlGenerator {

    private static final String BASE62_CHARACTERS = 
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int BASE = BASE62_CHARACTERS.length(); // 62
    private static final int TARGET_LENGTH = 7;

    private UrlGenerator() {
    }

    /**
     * Converts a unique 64-bit ID into a 7-character Base62 string.
     * Guaranteed zero collisions if the input IDs are unique.
     */
    public static String encode(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

        // Edge case: 0 maps to a fully padded code using the first alphabet char
        if (id == 0) {
            return String.valueOf(BASE62_CHARACTERS.charAt(0)).repeat(TARGET_LENGTH);
        }

        StringBuilder sb = new StringBuilder();

        while (id > 0) {
            int remainder = (int) (id % BASE);
            sb.append(BASE62_CHARACTERS.charAt(remainder));
            id /= BASE;
        }

        String encoded = sb.reverse().toString();
        return padLeft(encoded, TARGET_LENGTH, BASE62_CHARACTERS.charAt(0));
    }

    /**
     * Decodes a 7-character Base62 string back to the original unique 64-bit ID.
     */
    public static long decode(String shortUrl) {
        if (shortUrl == null || shortUrl.isBlank()) {
            throw new IllegalArgumentException("Short URL code must not be blank.");
        }

        long id = 0;

        for (int i = 0; i < shortUrl.length(); i++) {
            char c = shortUrl.charAt(i);
            int value = BASE62_CHARACTERS.indexOf(c);

            if (value == -1) {
                throw new IllegalArgumentException("Invalid character found in short URL: " + c);
            }

            id = (id * BASE) + value;
        }

        return id;
    }

    private static String padLeft(String input, int length, char padChar) {
        if (input.length() >= length) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        // Fixed condition to avoid inner length mutation tracking bugs
        int paddingNeeded = length - input.length();
        for (int i = 0; i < paddingNeeded; i++) {
            sb.append(padChar);
        }
        sb.append(input);
        return sb.toString();
    }

    public static int targetLength() {
        return TARGET_LENGTH;
    }

    public static String alphabet() {
        return BASE62_CHARACTERS;
    }
}
