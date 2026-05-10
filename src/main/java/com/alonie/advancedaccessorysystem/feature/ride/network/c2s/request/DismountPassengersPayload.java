package com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record DismountPassengersPayload(
        boolean useChargedLaunch,
        double chargedLaunchSpeed
) {
    public static final Identifier ID = Identifier.of(AdvancedAccessorySystemMod.MOD_ID, "dismount_passengers");

    public static void write(DismountPassengersPayload payload, PacketByteBuf buf) {
        buf.writeBoolean(payload.useChargedLaunch());
        buf.writeDouble(BoatPassengerConfigHelper.sanitizeDismountLaunchSpeed(payload.chargedLaunchSpeed()));
    }

    public static DismountPassengersPayload read(PacketByteBuf buf) {
        return new DismountPassengersPayload(
                buf.readBoolean(),
                buf.readDouble()
        );
    }
}
