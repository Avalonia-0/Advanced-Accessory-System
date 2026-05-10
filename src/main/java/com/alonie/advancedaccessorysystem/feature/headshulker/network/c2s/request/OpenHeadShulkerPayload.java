package com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record OpenHeadShulkerPayload() {
    public static final Identifier ID = Identifier.of(AdvancedAccessorySystemMod.MOD_ID, "open_head_shulker");

    public static void write(OpenHeadShulkerPayload payload, PacketByteBuf buf) {
    }

    public static OpenHeadShulkerPayload read(PacketByteBuf buf) {
        return new OpenHeadShulkerPayload();
    }
}
