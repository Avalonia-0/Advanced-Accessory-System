package com.alonie.advancedaccessorysystem.feature.boatpassenger.network.s2c.sync;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public record BoatPassengerSettingsSyncPayload(
        double radius,
        String boatAutoPickUpJson,
        String addedBoatIdsJson,
        String addedSaddleIdsJson,
        double dismountLaunchSpeed,
        String chargeJson
) {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "boat_passenger_settings_sync");

    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(BoatPassengerConfigHelper.sanitizeRadius(radius()));
        buf.writeUtf(boatAutoPickUpJson() == null
                ? BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON
                : boatAutoPickUpJson());
        buf.writeUtf(addedBoatIdsJson() == null
                ? BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON
                : addedBoatIdsJson());
        buf.writeUtf(addedSaddleIdsJson() == null
                ? BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON
                : addedSaddleIdsJson());
        buf.writeDouble(BoatPassengerConfigHelper.sanitizeDismountLaunchSpeed(dismountLaunchSpeed()));
        buf.writeUtf(chargeJson() == null
                ? BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON
                : chargeJson());
    }

    public static BoatPassengerSettingsSyncPayload read(FriendlyByteBuf buf) {
        return new BoatPassengerSettingsSyncPayload(
                buf.readDouble(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readUtf()
        );
    }
}
