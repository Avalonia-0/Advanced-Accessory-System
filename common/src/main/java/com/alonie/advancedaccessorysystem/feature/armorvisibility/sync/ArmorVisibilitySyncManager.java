package com.alonie.advancedaccessorysystem.feature.armorvisibility.sync;

import com.alonie.advancedaccessorysystem.bridge.PlatformNetworking;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request.ArmorVisibilityUpdatePayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync.ArmorVisibilitySyncPayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.state.ArmorVisibilityServerState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public final class ArmorVisibilitySyncManager {
    private static final long FULL_RESYNC_INTERVAL_TICKS = 40L;

    private ArmorVisibilitySyncManager() {
    }

    public static void handleUpdate(ServerPlayer player, ArmorVisibilityUpdatePayload payload) {
        if (player == null || payload == null) {
            return;
        }
        updateVisibilityMask(player, payload.visibilityMask(), player.level().getServer());
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ArmorVisibilityServerState.markPendingFullSync(player.getUUID());
        broadcast(player.level().getServer(), new ArmorVisibilitySyncPayload(player.getUUID(), 0));
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        if (player == null) {
            return;
        }
        UUID playerUuid = player.getUUID();
        ArmorVisibilityServerState.clearPlayer(playerUuid);
        broadcast(player.level().getServer(), new ArmorVisibilitySyncPayload(playerUuid, 0));
    }

    public static void resetRuntimeState() {
        ArmorVisibilityServerState.reset();
    }

    public static void onServerTick(MinecraftServer server) {
        long currentTick = ArmorVisibilityServerState.advanceTick();

        if (!ArmorVisibilityServerState.hasPendingFullSyncs()) {
            if (currentTick % FULL_RESYNC_INTERVAL_TICKS == 0L) {
                syncAllKnownStates(server);
            }
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerUuid = player.getUUID();
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

    private static void updateVisibilityMask(ServerPlayer player, int visibilityMask, MinecraftServer server) {
        int sanitizedMask = ArmorVisibilityMask.sanitize(visibilityMask);
        ArmorVisibilityServerState.setMask(player.getUUID(), sanitizedMask);

        broadcast(server, new ArmorVisibilitySyncPayload(player.getUUID(), sanitizedMask));
    }

    private static void syncKnownStatesTo(ServerPlayer player) {
        for (Map.Entry<UUID, Integer> entry : ArmorVisibilityServerState.masks()) {
            send(player, new ArmorVisibilitySyncPayload(entry.getKey(), entry.getValue()));
        }
    }

    private static void syncAllKnownStates(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncKnownStatesTo(player);
        }
    }

    private static void broadcast(MinecraftServer server, ArmorVisibilitySyncPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(player, payload);
        }
    }

    private static void send(ServerPlayer player, ArmorVisibilitySyncPayload payload) {
        RegistryFriendlyByteBuf buf = PlatformNetworking.createBuffer(player.registryAccess());
        payload.write(buf);
        PlatformNetworking.sendToClient(ArmorVisibilitySyncPayload.ID, buf, player);
    }
}
