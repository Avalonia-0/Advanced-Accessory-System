package com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public record OpenHeadShulkerPayload() {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "open_head_shulker");

    public void write(FriendlyByteBuf buf) {
    }

    public static OpenHeadShulkerPayload read(FriendlyByteBuf buf) {
        return new OpenHeadShulkerPayload();
    }
}
