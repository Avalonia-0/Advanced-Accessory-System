package com.alonie.advancedaccessorysystem.feature.ride.network.s2c.sync;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public record RideStateSyncPayload(int vehicleId, int passengerId, boolean mounted) {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "ride_state_sync");

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(vehicleId());
        buf.writeVarInt(passengerId());
        buf.writeBoolean(mounted());
    }

    public static RideStateSyncPayload read(FriendlyByteBuf buf) {
        return new RideStateSyncPayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }
}
