package com.alonie.advancedaccessorysystem.feature.accessory.state;

import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatAutoPickUpRules;
import net.minecraft.util.Identifier;

/**
 * Runtime pattern cache for configured accessory matching.
 * This is derived session state, not persisted per-player data.
 */
public final class AccessoryPatternRuntimeState {
    private static BoatAutoPickUpRules addedBoatRules = BoatAutoPickUpRules.createDefaultAddedBoatRules();
    private static BoatAutoPickUpRules addedSaddleRules = BoatAutoPickUpRules.createDefaultAddedSaddleRules();

    private AccessoryPatternRuntimeState() {
    }

    public static synchronized void setAddedBoatPatterns(BoatAutoPickUpRules rules) {
        addedBoatRules = rules == null ? BoatAutoPickUpRules.createDefaultAddedBoatRules() : rules;
    }

    public static synchronized void setAddedSaddlePatterns(BoatAutoPickUpRules rules) {
        addedSaddleRules = rules == null ? BoatAutoPickUpRules.createDefaultAddedSaddleRules() : rules;
    }

    public static boolean matchesBoat(Identifier itemId) {
        return addedBoatRules.allows(itemId);
    }

    public static boolean matchesSaddle(Identifier itemId) {
        return addedSaddleRules.allows(itemId);
    }
}
