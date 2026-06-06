package com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public record DismountPassengersPayload(
        boolean useChargedLaunch,
        double chargedLaunchSpeed
) {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "dismount_passengers");

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(useChargedLaunch());
        buf.writeDouble(BoatPassengerConfigHelper.sanitizeDismountLaunchSpeed(chargedLaunchSpeed()));
    }

    public static DismountPassengersPayload read(FriendlyByteBuf buf) {
        return new DismountPassengersPayload(
                buf.readBoolean(),
                buf.readDouble()
        );
    }
}
