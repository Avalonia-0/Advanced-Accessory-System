package com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record ArmorVisibilitySyncPayload(UUID playerUuid, int visibilityMask) {
    public static final Identifier ID = Identifier.of(AdvancedAccessorySystemMod.MOD_ID, "armor_visibility_sync");

    public static void write(ArmorVisibilitySyncPayload payload, PacketByteBuf buf) {
        buf.writeUuid(payload.playerUuid());
        buf.writeByte(ArmorVisibilityMask.sanitize(payload.visibilityMask()));
    }

    public static ArmorVisibilitySyncPayload read(PacketByteBuf buf) {
        return new ArmorVisibilitySyncPayload(buf.readUuid(), buf.readByte() & 0xFF);
    }
}
