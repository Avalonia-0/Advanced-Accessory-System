package com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public record ArmorVisibilityUpdatePayload(int visibilityMask) {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "armor_visibility_update");

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(ArmorVisibilityMask.sanitize(visibilityMask()));
    }

    public static ArmorVisibilityUpdatePayload read(FriendlyByteBuf buf) {
        return new ArmorVisibilityUpdatePayload(buf.readByte() & 0xFF);
    }
}
