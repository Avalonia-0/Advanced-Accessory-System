package com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request;

import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;
import net.minecraft.server.network.ServerPlayerEntity;

public final class DismountPassengersRequestHandler {
    private DismountPassengersRequestHandler() {
    }

    public static void handleRequest(ServerPlayerEntity player, DismountPassengersPayload payload) {
        if (player == null || !player.isAlive() || player.isSpectator() || player.isSleeping()) {
            return;
        }

        if (!PlayerRideSyncManager.forceDismountPassengersFromHotkey(
                player,
                payload != null && payload.useChargedLaunch(),
                payload == null ? 0.0D : payload.chargedLaunchSpeed()
        )) {
            return;
        }
    }
}
