package com.alonie.advancedaccessorysystem.feature.ride.network.s2c.sync;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record RideStateSyncPayload(int vehicleId, int passengerId, boolean mounted) {
    public static final Identifier ID = Identifier.of(AdvancedAccessorySystemMod.MOD_ID, "ride_state_sync");

    public static void write(RideStateSyncPayload payload, PacketByteBuf buf) {
        buf.writeVarInt(payload.vehicleId());
        buf.writeVarInt(payload.passengerId());
        buf.writeBoolean(payload.mounted());
    }

    public static RideStateSyncPayload read(PacketByteBuf buf) {
        return new RideStateSyncPayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }
}
