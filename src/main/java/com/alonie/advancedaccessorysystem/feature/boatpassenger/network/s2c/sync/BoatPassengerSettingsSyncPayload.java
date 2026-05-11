package com.alonie.advancedaccessorysystem.feature.boatpassenger.network.s2c.sync;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record BoatPassengerSettingsSyncPayload(
        double radius,
        String boatAutoPickUpJson,
        String addedBoatIdsJson,
        String addedSaddleIdsJson,
        double dismountLaunchSpeed,
        String chargeJson
) {
    public static final Identifier ID = Identifier.of(AdvancedAccessorySystemMod.MOD_ID, "boat_passenger_settings_sync");

    public static void write(BoatPassengerSettingsSyncPayload payload, PacketByteBuf buf) {
        buf.writeDouble(BoatPassengerConfigHelper.sanitizeRadius(payload.radius()));
        buf.writeString(payload.boatAutoPickUpJson() == null
                ? BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON
                : payload.boatAutoPickUpJson());
        buf.writeString(payload.addedBoatIdsJson() == null
                ? BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON
                : payload.addedBoatIdsJson());
        buf.writeString(payload.addedSaddleIdsJson() == null
                ? BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON
                : payload.addedSaddleIdsJson());
        buf.writeDouble(BoatPassengerConfigHelper.sanitizeDismountLaunchSpeed(payload.dismountLaunchSpeed()));
        buf.writeString(payload.chargeJson() == null
                ? BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON
                : payload.chargeJson());
    }

    public static BoatPassengerSettingsSyncPayload read(PacketByteBuf buf) {
        return new BoatPassengerSettingsSyncPayload(
                buf.readDouble(),
                buf.readString(),
                buf.readString(),
                buf.readString(),
                buf.readDouble(),
                buf.readString()
        );
    }
}
