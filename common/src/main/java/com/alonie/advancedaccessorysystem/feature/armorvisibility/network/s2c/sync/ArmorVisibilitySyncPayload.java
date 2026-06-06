package com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record ArmorVisibilitySyncPayload(UUID playerUuid, int visibilityMask) {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "armor_visibility_sync");

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid());
        buf.writeByte(ArmorVisibilityMask.sanitize(visibilityMask()));
    }

    public static ArmorVisibilitySyncPayload read(FriendlyByteBuf buf) {
        return new ArmorVisibilitySyncPayload(buf.readUUID(), buf.readByte() & 0xFF);
    }
}
