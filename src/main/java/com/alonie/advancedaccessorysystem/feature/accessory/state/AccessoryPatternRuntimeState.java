package com.alonie.advancedaccessorysystem.feature.accessory.state;

import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Runtime pattern cache for configured accessory matching.
 * This is derived session state, not persisted per-player data.
 */
public final class AccessoryPatternRuntimeState {
    private static Set<String> addedBoatPatterns = BoatPassengerConfigHelper.getDefaultAddedBoatPatterns();
    private static Set<String> addedSaddlePatterns = BoatPassengerConfigHelper.getDefaultAddedSaddlePatterns();

    private AccessoryPatternRuntimeState() {
    }

    public static synchronized void setAddedBoatPatterns(Set<String> patterns) {
        addedBoatPatterns = Collections.unmodifiableSet(new LinkedHashSet<>(patterns));
    }

    public static synchronized void setAddedSaddlePatterns(Set<String> patterns) {
        addedSaddlePatterns = Collections.unmodifiableSet(new LinkedHashSet<>(patterns));
    }

    public static boolean matchesBoat(Identifier itemId) {
        return matches(itemId, addedBoatPatterns);
    }

    public static boolean matchesSaddle(Identifier itemId) {
        return matches(itemId, addedSaddlePatterns);
    }

    private static boolean matches(Identifier itemId, Set<String> patterns) {
        if (itemId == null) {
            return false;
        }

        String fullId = itemId.toString();
        String path = itemId.getPath();

        for (String pattern : patterns) {
            if (matchesPattern(pattern, fullId, path)) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesPattern(String rawPattern, String fullId, String path) {
        String pattern = BoatPassengerConfigHelper.normalizeIdentifier(rawPattern);
        if (pattern.isEmpty()) {
            return false;
        }

        String candidate = pattern.contains(":") ? fullId : path;
        return Pattern.compile(toRegex(pattern)).matcher(candidate).matches();
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
