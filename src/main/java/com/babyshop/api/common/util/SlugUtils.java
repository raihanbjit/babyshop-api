package com.babyshop.api.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs.
 *
 * Slug generation rules:
 * - Convert to lowercase
 * - Remove accents/diacritics
 * - Replace spaces and special chars with hyphens
 * - Remove consecutive hyphens
 * - Trim hyphens from start/end
 *
 * Examples:
 * - "Baby Shoes" → "baby-shoes"
 * - "Toys & Games!" → "toys-games"
 * - "Café Racer" → "cafe-racer"
 */
public final class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-|-$)");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-{2,}");

    private SlugUtils() {
        // Utility class
    }

    /**
     * Generate a URL-friendly slug from input string.
     *
     * @param input input string
     * @return slug or null if input is null/empty
     */
    public static String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        // Normalize to decomposed form to handle accents
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Convert to lowercase
        String lowercase = normalized.toLowerCase(Locale.ENGLISH);

        // Replace whitespace with hyphens
        String hyphenated = WHITESPACE.matcher(lowercase).replaceAll("-");

        // Remove all non-word characters except hyphens
        String cleaned = NON_LATIN.matcher(hyphenated).replaceAll("");

        // Replace multiple consecutive hyphens with single hyphen
        String singleHyphens = MULTIPLE_DASHES.matcher(cleaned).replaceAll("-");

        // Remove hyphens from start and end
        String trimmed = EDGES_DASHES.matcher(singleHyphens).replaceAll("");

        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Validate if a string is a valid slug.
     *
     * @param slug slug to validate
     * @return true if valid slug format
     */
    public static boolean isValidSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }
        return slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }
}

