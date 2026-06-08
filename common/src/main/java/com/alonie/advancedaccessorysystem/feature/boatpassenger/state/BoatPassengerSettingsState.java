package com.alonie.advancedaccessorysystem.feature.boatpassenger.state;

import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatAutoPickUpRules;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public record BoatPassengerSettingsState(
        double radius,
        BoatAutoPickUpRules autoPickUpRules
) {
    public static final BoatPassengerSettingsState DEFAULT = of(
            BoatPassengerConfigHelper.DEFAULT_AUTO_RIDE_RADIUS,
            BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON
    );

    public static BoatPassengerSettingsState of(
            double radius,
            String boatAutoPickUpJson
    ) {
        return new BoatPassengerSettingsState(
                BoatPassengerConfigHelper.sanitizeRadius(radius),
                BoatAutoPickUpRules.parse(boatAutoPickUpJson)
        );
    }

    public BoatPassengerSettingsState {
        autoPickUpRules = autoPickUpRules == null ? BoatAutoPickUpRules.createDefault() : autoPickUpRules.copy();
    }

    public boolean allowsAutoPickUp(Entity entity) {
        Identifier entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return this.autoPickUpRules.allows(entityTypeId);
    }
}
