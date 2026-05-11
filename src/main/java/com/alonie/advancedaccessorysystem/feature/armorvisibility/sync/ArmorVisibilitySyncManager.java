package com.alonie.advancedaccessorysystem.feature.armorvisibility.sync;

import com.alonie.advancedaccessorysystem.collective.CollectiveNetworkingBridge;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request.ArmorVisibilityUpdatePayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync.ArmorVisibilitySyncPayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.state.ArmorVisibilityServerState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public final class ArmorVisibilitySyncManager {
    private static final long FULL_RESYNC_INTERVAL_TICKS = 40L;

    private ArmorVisibilitySyncManager() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ArmorVisibilitySyncManager::onEndServerTick);
    }

    public static void handleUpdate(ServerPlayerEntity player, ArmorVisibilityUpdatePayload payload) {
        if (player == null || payload == null) {
            return;
        }
        updateVisibilityMask(player, payload.visibilityMask(), player.getEntityWorld().getServer());
    }

    public static void onPlayerLoggedIn(ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        ArmorVisibilityServerState.markPendingFullSync(player.getUuid());
        broadcast(player.getEntityWorld().getServer(), new ArmorVisibilitySyncPayload(player.getUuid(), 0));
    }

    public static void onPlayerLoggedOut(ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        UUID playerUuid = player.getUuid();
        ArmorVisibilityServerState.clearPlayer(playerUuid);
        broadcast(player.getEntityWorld().getServer(), new ArmorVisibilitySyncPayload(playerUuid, 0));
    }

    public static void resetRuntimeState() {
        ArmorVisibilityServerState.reset();
    }

    private static void onEndServerTick(MinecraftServer server) {
        long currentTick = ArmorVisibilityServerState.advanceTick();

        if (!ArmorVisibilityServerState.hasPendingFullSyncs()) {
            if (currentTick % FULL_RESYNC_INTERVAL_TICKS == 0L) {
                syncAllKnownStates(server);
            }
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID playerUuid = player.getUuid();
            if (!ArmorVisibilityServerState.isPendingFullSync(playerUuid)) {
                continue;
            }

            syncKnownStatesTo(player);
            ArmorVisibilityServerState.clearPendingFullSync(playerUuid);
        }

        if (currentTick % FULL_RESYNC_INTERVAL_TICKS == 0L) {
            syncAllKnownStates(server);
        }
    }

    private static void updateVisibilityMask(ServerPlayerEntity player, int visibilityMask, MinecraftServer server) {
        int sanitizedMask = ArmorVisibilityMask.sanitize(visibilityMask);
        ArmorVisibilityServerState.setMask(player.getUuid(), sanitizedMask);

        broadcast(server, new ArmorVisibilitySyncPayload(player.getUuid(), sanitizedMask));
    }

    private static void syncKnownStatesTo(ServerPlayerEntity player) {
        for (Map.Entry<UUID, Integer> entry : ArmorVisibilityServerState.masks()) {
            send(player, new ArmorVisibilitySyncPayload(entry.getKey(), entry.getValue()));
        }
    }

    private static void syncAllKnownStates(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            syncKnownStatesTo(player);
        }
    }

    private static void broadcast(MinecraftServer server, ArmorVisibilitySyncPayload payload) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            send(player, payload);
        }
    }

    private static void send(ServerPlayerEntity player, ArmorVisibilitySyncPayload payload) {
        CollectiveNetworkingBridge.sendToClient(payload, player);
    }
}
