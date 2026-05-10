package com.alonie.advancedaccessorysystem.feature.boatpassenger.state;

import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatAutoPickUpRules;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.ChargeConfigData;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record BoatPassengerSettingsState(
        double radius,
        BoatAutoPickUpRules autoPickUpRules,
        double dismountLaunchSpeed,
        ChargeConfigData chargeConfig
) {
    public static final BoatPassengerSettingsState DEFAULT = of(
            BoatPassengerConfigHelper.DEFAULT_AUTO_RIDE_RADIUS,
            BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON,
            BoatPassengerConfigHelper.DEFAULT_DISMOUNT_LAUNCH_SPEED,
            BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON
    );

    public static BoatPassengerSettingsState of(
            double radius,
            String boatAutoPickUpJson,
            double dismountLaunchSpeed,
            String chargeJson
    ) {
        return new BoatPassengerSettingsState(
                BoatPassengerConfigHelper.sanitizeRadius(radius),
                BoatAutoPickUpRules.parse(boatAutoPickUpJson),
                BoatPassengerConfigHelper.sanitizeDismountLaunchSpeed(dismountLaunchSpeed),
                ChargeConfigData.of(chargeJson)
        );
    }

    public BoatPassengerSettingsState {
        autoPickUpRules = autoPickUpRules == null ? BoatAutoPickUpRules.createDefault() : autoPickUpRules.copy();
        chargeConfig = chargeConfig == null ? ChargeConfigData.DEFAULT : chargeConfig;
    }

    public boolean allowsAutoPickUp(Entity entity) {
        Identifier entityTypeId = Registries.ENTITY_TYPE.getId(entity.getType());
        return this.autoPickUpRules.allows(entityTypeId);
    }

    public boolean allowsChargedLaunch(Entity entity) {
        return this.chargeConfig.allows(entity);
    }
}
