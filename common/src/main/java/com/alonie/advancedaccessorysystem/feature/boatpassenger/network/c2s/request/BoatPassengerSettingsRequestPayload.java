package com.alonie.advancedaccessorysystem.feature.boatpassenger.network.c2s.request;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public record BoatPassengerSettingsRequestPayload(
        double radius,
        String boatAutoPickUpJson,
        String addedBoatIdsJson,
        String addedSaddleIdsJson
) {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "boat_passenger_settings_request");

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
    }

    public static BoatPassengerSettingsRequestPayload read(FriendlyByteBuf buf) {
        return new BoatPassengerSettingsRequestPayload(
                buf.readDouble(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf()
        );
    }
}
