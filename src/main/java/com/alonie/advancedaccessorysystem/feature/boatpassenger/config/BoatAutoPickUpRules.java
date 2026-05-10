package com.alonie.advancedaccessorysystem.feature.boatpassenger.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class BoatAutoPickUpRules {
    private static final String ALLOWED_KEY = "allowed";
    private static final String LEGACY_ALLOWED_KEY = "allow";
    private static final String EXCLUDED_KEY = "excluded";
    private static final String LEGACY_EXCLUDED_KEY = "exclude";

    private final LinkedHashSet<String> allowedPatterns = new LinkedHashSet<>();
    private final LinkedHashMap<String, LinkedHashSet<String>> excludedByWildcard = new LinkedHashMap<>();

    private BoatAutoPickUpRules() {
    }

    public static BoatAutoPickUpRules createDefault() {
        BoatAutoPickUpRules rules = new BoatAutoPickUpRules();
        rules.allowedPatterns.addAll(BoatPassengerConfigHelper.getDefaultAutoPickUpAllowedPatterns());
        return rules;
    }

    public static BoatAutoPickUpRules createDefaultChargeRules() {
        BoatAutoPickUpRules rules = new BoatAutoPickUpRules();
        rules.allowedPatterns.addAll(BoatPassengerConfigHelper.getDefaultChargeAllowedPatterns());
        return rules;
    }

    public static BoatAutoPickUpRules parse(String rawJson) {
        return parse(rawJson, BoatPassengerConfigHelper.getDefaultAutoPickUpAllowedPatterns());
    }

    public static BoatAutoPickUpRules parseChargeRules(String rawJson) {
        return parse(rawJson, BoatPassengerConfigHelper.getDefaultChargeAllowedPatterns());
    }

    public static BoatAutoPickUpRules parse(String rawJson, Set<String> defaultAllowedPatterns) {
        if (rawJson == null || rawJson.isBlank()) {
            return create(defaultAllowedPatterns);
        }

        try {
            return parse(JsonParser.parseString(rawJson), defaultAllowedPatterns);
        } catch (JsonParseException ignored) {
            return create(defaultAllowedPatterns);
        }
    }

    public static BoatAutoPickUpRules parse(JsonElement element) {
        return parse(element, BoatPassengerConfigHelper.getDefaultAutoPickUpAllowedPatterns());
    }

    public static BoatAutoPickUpRules parseChargeRules(JsonElement element) {
        return parse(element, BoatPassengerConfigHelper.getDefaultChargeAllowedPatterns());
    }

    public static BoatAutoPickUpRules parse(JsonElement element, Set<String> defaultAllowedPatterns) {
        if (element == null || element.isJsonNull()) {
            return create(defaultAllowedPatterns);
        }

        if (element instanceof JsonArray array) {
            BoatAutoPickUpRules rules = new BoatAutoPickUpRules();
            rules.allowedPatterns.addAll(BoatPassengerConfigHelper.parseStringArray(
                    array,
                    defaultAllowedPatterns
            ));
            rules.sanitize();
            return rules;
        }

        if (!(element instanceof JsonObject object)) {
            return create(defaultAllowedPatterns);
        }

        BoatAutoPickUpRules rules = new BoatAutoPickUpRules();
        JsonArray allowedArray = getJsonArray(object, ALLOWED_KEY, LEGACY_ALLOWED_KEY);
        rules.allowedPatterns.addAll(BoatPassengerConfigHelper.parseStringArray(
                allowedArray,
                defaultAllowedPatterns
        ));

        JsonObject excludedObject = getJsonObject(object, EXCLUDED_KEY, LEGACY_EXCLUDED_KEY);
        if (excludedObject != null) {
            for (Map.Entry<String, JsonElement> entry : excludedObject.entrySet()) {
                String wildcardPattern = BoatPassengerConfigHelper.normalizeIdentifier(entry.getKey());
                if (!(entry.getValue() instanceof JsonArray excludedArray)) {
                    continue;
                }

                LinkedHashSet<String> excludedIds = new LinkedHashSet<>();
                for (JsonElement excludedEntry : excludedArray) {
                    if (!excludedEntry.isJsonPrimitive() || !excludedEntry.getAsJsonPrimitive().isString()) {
                        continue;
                    }

                    String excludedId = BoatPassengerConfigHelper.normalizeIdentifier(excludedEntry.getAsString());
                    if (Identifier.tryParse(excludedId) != null) {
                        excludedIds.add(excludedId);
                    }
                }

                if (!excludedIds.isEmpty()) {
                    rules.excludedByWildcard.put(wildcardPattern, excludedIds);
                }
            }
        }

        rules.sanitize();
        return rules;
    }

    private static JsonArray getJsonArray(JsonObject object, String primaryKey, String fallbackKey) {
        if (object.get(primaryKey) instanceof JsonArray jsonArray) {
            return jsonArray;
        }

        return object.get(fallbackKey) instanceof JsonArray jsonArray ? jsonArray : null;
    }

    private static JsonObject getJsonObject(JsonObject object, String primaryKey, String fallbackKey) {
        if (object.get(primaryKey) instanceof JsonObject jsonObject) {
            return jsonObject;
        }

        return object.get(fallbackKey) instanceof JsonObject jsonObject ? jsonObject : null;
    }

    private static BoatAutoPickUpRules create(Set<String> defaultAllowedPatterns) {
        BoatAutoPickUpRules rules = new BoatAutoPickUpRules();
        rules.allowedPatterns.addAll(defaultAllowedPatterns);
        return rules;
    }

    public BoatAutoPickUpRules copy() {
        BoatAutoPickUpRules copy = new BoatAutoPickUpRules();
        copy.allowedPatterns.addAll(this.allowedPatterns);
        for (Map.Entry<String, LinkedHashSet<String>> entry : this.excludedByWildcard.entrySet()) {
            copy.excludedByWildcard.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
        }
        return copy;
    }

    public ToggleResult toggle(Identifier entityId) {
        String exactId = BoatPassengerConfigHelper.normalizeExactIdentifier(entityId);
        if (exactId.isEmpty()) {
            return ToggleResult.NONE;
        }

        if (allows(entityId)) {
            boolean changed = this.allowedPatterns.remove(exactId);
            for (String wildcardPattern : getMatchingWildcardPatterns(entityId)) {
                changed |= this.excludedByWildcard
                        .computeIfAbsent(wildcardPattern, key -> new LinkedHashSet<>())
                        .add(exactId);
            }

            sanitize();
            return changed ? ToggleResult.REMOVED : ToggleResult.NONE;
        }

        boolean changed = false;
        for (LinkedHashSet<String> excludedIds : this.excludedByWildcard.values()) {
            changed |= excludedIds.remove(exactId);
        }

        sanitize();
        if (!allows(entityId)) {
            changed |= this.allowedPatterns.add(exactId);
        }

        sanitize();
        return changed ? ToggleResult.ADDED : ToggleResult.NONE;
    }

    public boolean allows(Identifier entityId) {
        if (entityId == null) {
            return false;
        }

        String exactId = entityId.toString();
        if (this.allowedPatterns.contains(exactId)) {
            return true;
        }

        for (String pattern : this.allowedPatterns) {
            if (!BoatPassengerConfigHelper.isWildcardPattern(pattern)) {
                continue;
            }

            if (BoatPassengerConfigHelper.matchesPattern(pattern, entityId)
                    && !this.excludedByWildcard.getOrDefault(pattern, new LinkedHashSet<>()).contains(exactId)) {
                return true;
            }
        }

        return false;
    }

    public JsonObject toJsonObject() {
        sanitize();

        JsonObject rootObject = new JsonObject();
        JsonArray allowedArray = new JsonArray();
        for (String pattern : this.allowedPatterns) {
            allowedArray.add(pattern);
        }
        rootObject.add(ALLOWED_KEY, allowedArray);

        JsonObject excludedObject = new JsonObject();
        for (Map.Entry<String, LinkedHashSet<String>> entry : this.excludedByWildcard.entrySet()) {
            JsonArray excludedArray = new JsonArray();
            for (String excludedId : entry.getValue()) {
                excludedArray.add(excludedId);
            }

            excludedObject.add(entry.getKey(), excludedArray);
        }
        rootObject.add(EXCLUDED_KEY, excludedObject);
        return rootObject;
    }

    public String toJsonString() {
        return toJsonObject().toString();
    }

    public Set<String> getAllowedPatterns() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(this.allowedPatterns));
    }

    public Map<String, Set<String>> getExcludedByWildcard() {
        LinkedHashMap<String, Set<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : this.excludedByWildcard.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableSet(new LinkedHashSet<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    private Set<String> getMatchingWildcardPatterns(Identifier entityId) {
        LinkedHashSet<String> matches = new LinkedHashSet<>();
        for (String pattern : this.allowedPatterns) {
            if (BoatPassengerConfigHelper.isWildcardPattern(pattern)
                    && BoatPassengerConfigHelper.matchesPattern(pattern, entityId)) {
                matches.add(pattern);
            }
        }
        return matches;
    }

    private void sanitize() {
        LinkedHashSet<String> normalizedAllowedPatterns = new LinkedHashSet<>();
        for (String pattern : this.allowedPatterns) {
            String normalizedPattern = BoatPassengerConfigHelper.normalizeIdentifier(pattern);
            if (BoatPassengerConfigHelper.isValidPattern(normalizedPattern)) {
                normalizedAllowedPatterns.add(normalizedPattern);
            }
        }

        LinkedHashMap<String, LinkedHashSet<String>> normalizedExcludedByWildcard = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : this.excludedByWildcard.entrySet()) {
            String wildcardPattern = BoatPassengerConfigHelper.normalizeIdentifier(entry.getKey());
            if (!BoatPassengerConfigHelper.isWildcardPattern(wildcardPattern)) {
                continue;
            }

            LinkedHashSet<String> excludedIds = new LinkedHashSet<>();
            for (String excludedId : entry.getValue()) {
                String normalizedExcludedId = BoatPassengerConfigHelper.normalizeIdentifier(excludedId);
                if (Identifier.tryParse(normalizedExcludedId) != null) {
                    excludedIds.add(normalizedExcludedId);
                }
            }

            if (!excludedIds.isEmpty()) {
                normalizedExcludedByWildcard.put(wildcardPattern, excludedIds);
            }
        }

        this.allowedPatterns.clear();
        this.allowedPatterns.addAll(normalizedAllowedPatterns);
        this.excludedByWildcard.clear();
        this.excludedByWildcard.putAll(normalizedExcludedByWildcard);
    }

    public enum ToggleResult {
        NONE,
        ADDED,
        REMOVED
    }
}
