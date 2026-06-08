package com.alonie.advancedaccessorysystem.feature.boatpassenger.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class BoatPassengerConfigHelper {
    public static final double DEFAULT_AUTO_RIDE_RADIUS = 2.0D;

    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    public static final String DEFAULT_BOAT_AUTO_PICK_UP_JSON =
            "{\"allowed\":[\"minecraft:villager\",\"minecraft:player\",\"*_boat\",\"*_raft\",\"minecraft:minecart\",\"minecraft:tnt_minecart\",\"minecraft:tnt\",\"minecraft:end_crystal\",\"minecraft:falling_block\"],\"excluded\":{}}";
    public static final String DEFAULT_ADDED_BOAT_IDS_JSON =
            "{\"allowed\":[\"*_boat\",\"*_raft\",\"minecraft:minecart\"],\"excluded\":{}}";
    public static final String DEFAULT_ADDED_SADDLE_IDS_JSON =
            "{\"allowed\":[\"saddle\",\"*_harness\"],\"excluded\":{}}";

    private static final Set<String> DEFAULT_AUTO_PICK_UP_ALLOWED_PATTERNS =
            Collections.unmodifiableSet(new LinkedHashSet<>(List.of(
                    "minecraft:villager",
                    "minecraft:player",
                    "*_boat",
                    "*_raft",
                    "minecraft:minecart",
                    "minecraft:tnt_minecart",
                    "minecraft:tnt",
                    "minecraft:falling_block"
            )));
    private static final Set<String> DEFAULT_ADDED_BOAT_PATTERNS =
            Collections.unmodifiableSet(new LinkedHashSet<>(List.of(
                    "*_boat",
                    "*_raft",
                    "minecraft:minecart"
            )));
    private static final Set<String> DEFAULT_ADDED_SADDLE_PATTERNS =
            Collections.unmodifiableSet(new LinkedHashSet<>(List.of(
                    "saddle",
                    "*_harness"
            )));

    private BoatPassengerConfigHelper() {
    }

    public static double sanitizeRadius(double radius) {
        return Math.max(0.0D, radius);
    }

    public static Set<String> getDefaultAutoPickUpAllowedPatterns() {
        return DEFAULT_AUTO_PICK_UP_ALLOWED_PATTERNS;
    }

    public static Set<String> getDefaultAddedBoatPatterns() {
        return DEFAULT_ADDED_BOAT_PATTERNS;
    }

    public static Set<String> getDefaultAddedSaddlePatterns() {
        return DEFAULT_ADDED_SADDLE_PATTERNS;
    }

    public static BoatAutoPickUpRules parseAddedBoatPatterns(String rawJson) {
        return BoatAutoPickUpRules.parse(rawJson, DEFAULT_ADDED_BOAT_PATTERNS);
    }

    public static BoatAutoPickUpRules parseAddedSaddlePatterns(String rawJson) {
        return BoatAutoPickUpRules.parse(rawJson, DEFAULT_ADDED_SADDLE_PATTERNS);
    }

    public static Set<String> parseStringArray(String rawJson, Set<String> fallbackValues) {
        if (rawJson == null || rawJson.isBlank()) {
            return fallbackValues;
        }

        try {
            JsonElement element = JsonParser.parseString(rawJson);
            if (!(element instanceof JsonArray array)) {
                return fallbackValues;
            }

            return parseStringArray(array, fallbackValues);
        } catch (JsonParseException ignored) {
            return fallbackValues;
        }
    }

    public static Set<String> parseStringArray(JsonArray array, Set<String> fallbackValues) {
        if (array == null) {
            return fallbackValues;
        }

        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (JsonElement entry : array) {
            if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()) {
                continue;
            }

            String value = normalizeIdentifier(entry.getAsString());
            if (isValidPattern(value)) {
                values.add(value);
            }
        }

        return Collections.unmodifiableSet(values);
    }

    public static String normalizeIdentifier(String rawId) {
        return rawId == null ? "" : rawId.trim().replaceAll("\\s+", "");
    }

    public static String normalizeExactIdentifier(Identifier id) {
        return id == null ? "" : id.toString();
    }

    public static String normalizeEntityId(String rawId) {
        return normalizeIdentifier(rawId);
    }

    public static boolean isWildcardPattern(String pattern) {
        return normalizeIdentifier(pattern).contains("*");
    }

    public static boolean isValidPattern(String pattern) {
        String normalized = normalizeIdentifier(pattern);
        if (normalized.isEmpty()) {
            return false;
        }

        return isWildcardPattern(normalized) || Identifier.tryParse(normalized) != null;
    }

    public static boolean matchesPattern(String rawPattern, Identifier id) {
        if (id == null) {
            return false;
        }

        String pattern = normalizeIdentifier(rawPattern);
        if (!isValidPattern(pattern)) {
            return false;
        }

        String fullId = id.toString();
        String path = id.getPath();
        String candidate = pattern.contains(":") ? fullId : path;
        return PATTERN_CACHE.computeIfAbsent(pattern, p -> Pattern.compile(toRegex(p)))
                .matcher(candidate).matches();
    }

    public static boolean isMalformedJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return false;
        }

        try {
            JsonParser.parseString(rawJson);
            return false;
        } catch (JsonParseException ignored) {
            return true;
        }
    }

    private static String toRegex(String wildcardPattern) {
        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < wildcardPattern.length(); i++) {
            char character = wildcardPattern.charAt(i);
            if (character == '*') {
                regex.append(".*");
            } else {
                regex.append(Pattern.quote(String.valueOf(character)));
            }
        }

        regex.append('$');
        return regex.toString();
    }
}
