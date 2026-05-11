package com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record ArmorVisibilityUpdatePayload(int visibilityMask) {
    public static final Identifier ID = Identifier.of(AdvancedAccessorySystemMod.MOD_ID, "armor_visibility_update");

    public static void write(ArmorVisibilityUpdatePayload payload, PacketByteBuf buf) {
        buf.writeByte(ArmorVisibilityMask.sanitize(payload.visibilityMask()));
    }

    public static ArmorVisibilityUpdatePayload read(PacketByteBuf buf) {
        return new ArmorVisibilityUpdatePayload(buf.readByte() & 0xFF);
    }
}
