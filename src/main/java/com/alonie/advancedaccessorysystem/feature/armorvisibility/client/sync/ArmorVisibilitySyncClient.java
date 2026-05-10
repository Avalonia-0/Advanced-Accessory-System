package com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync;

import com.alonie.advancedaccessorysystem.collective.CollectiveNetworkingBridge;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorVisibilityClientCache;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorSlotVisibilityState;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request.ArmorVisibilityUpdatePayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync.ArmorVisibilitySyncPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.PlayerLikeEntity;

public final class ArmorVisibilitySyncClient {
    private static final long LOCAL_MASK_RESEND_INTERVAL_TICKS = 40L;

    private ArmorVisibilitySyncClient() {
    }

    public static void register() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ArmorVisibilityClientCache.reset());
        ClientTickEvents.END_CLIENT_TICK.register(ArmorVisibilitySyncClient::onEndClientTick);
    }

    public static void onClientWorldLoad() {
        ArmorVisibilityClientCache.reset();
    }

    public static int getMask(PlayerLikeEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && player.getUuid().equals(client.player.getUuid())) {
            return ArmorSlotVisibilityState.getLocalMask();
        }

        return ArmorVisibilityClientCache.getSyncedMask(player.getUuid());
    }

    public static void onLocalConfigChanged() {
        ArmorVisibilityClientCache.markLocalStateDirty();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            ArmorVisibilityClientCache.setSyncedMask(client.player.getUuid(), ArmorSlotVisibilityState.getLocalMask());
        }
    }

    private static void onEndClientTick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            ArmorVisibilityClientCache.reset();
            return;
        }

        ArmorVisibilityClientCache.advanceClientTick();
        syncLocalMask(client);
    }

    private static void syncLocalMask(MinecraftClient client) {
        int localMask = ArmorSlotVisibilityState.getLocalMask();
        boolean resendDue = ArmorVisibilityClientCache.lastSentTick() == Long.MIN_VALUE
                || ArmorVisibilityClientCache.currentClientTick() - ArmorVisibilityClientCache.lastSentTick() >= LOCAL_MASK_RESEND_INTERVAL_TICKS;
        if (!ArmorVisibilityClientCache.isLocalStateDirty()
                && localMask == ArmorVisibilityClientCache.lastSentMask()
                && !resendDue) {
            return;
        }

        ArmorVisibilityClientCache.setSyncedMask(client.player.getUuid(), localMask);

        CollectiveNetworkingBridge.sendToServer(new ArmorVisibilityUpdatePayload(localMask));
        ArmorVisibilityClientCache.setLastSentMask(localMask);
        ArmorVisibilityClientCache.clearLocalStateDirty();
        ArmorVisibilityClientCache.setLastSentTick(ArmorVisibilityClientCache.currentClientTick());
    }

    public static void handleSync(ArmorVisibilitySyncPayload payload) {
        ArmorVisibilityClientCache.setSyncedMask(payload.playerUuid(), payload.visibilityMask());
    }
}
