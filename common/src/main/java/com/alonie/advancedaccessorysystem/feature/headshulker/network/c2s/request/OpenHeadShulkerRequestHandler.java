package com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request;

import com.alonie.advancedaccessorysystem.feature.headshulker.logic.ShulkerBoxCompat;
import net.minecraft.server.level.ServerPlayer;

public final class OpenHeadShulkerRequestHandler {
    private OpenHeadShulkerRequestHandler() {
    }

    public static void handleRequest(ServerPlayer player, OpenHeadShulkerPayload payload) {
        if (player == null || !player.isAlive() || player.isSpectator() || player.isSleeping()) {
            return;
        }
        if (player.containerMenu != player.inventoryMenu) {
            return;
        }

        ShulkerBoxCompat.openHeadEquippedShulker(player);
    }
}
