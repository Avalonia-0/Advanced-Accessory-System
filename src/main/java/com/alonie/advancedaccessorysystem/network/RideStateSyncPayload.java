package com.alonie.advancedaccessorysystem.network;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RideStateSyncPayload(int vehicleId, int passengerId, boolean mounted) implements CustomPayload {
    public static final Id<RideStateSyncPayload> ID = new Id<>(Identifier.of(AdvancedAccessorySystemMod.MOD_ID, "ride_state_sync"));
    public static final PacketCodec<RegistryByteBuf, RideStateSyncPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeVarInt(payload.vehicleId());
                buf.writeVarInt(payload.passengerId());
                buf.writeBoolean(payload.mounted());
            },
            buf -> new RideStateSyncPayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
