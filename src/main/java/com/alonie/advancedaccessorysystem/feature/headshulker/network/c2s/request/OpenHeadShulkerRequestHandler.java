package com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request;

import com.alonie.advancedaccessorysystem.feature.headshulker.logic.ShulkerBoxCompat;
import net.minecraft.server.network.ServerPlayerEntity;

public final class OpenHeadShulkerRequestHandler {
    private OpenHeadShulkerRequestHandler() {
    }

    public static void handleRequest(ServerPlayerEntity player, OpenHeadShulkerPayload payload) {
        if (player == null || !player.isAlive() || player.isSpectator() || player.isSleeping()) {
            return;
        }
        if (player.currentScreenHandler != player.playerScreenHandler) {
            return;
        }

        ShulkerBoxCompat.openHeadEquippedShulker(player);
    }
}
