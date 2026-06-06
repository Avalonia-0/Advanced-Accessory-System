package com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync;

import com.alonie.advancedaccessorysystem.bridge.PlatformNetworking;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorVisibilityClientCache;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorSlotVisibilityState;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request.ArmorVisibilityUpdatePayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync.ArmorVisibilitySyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public final class ArmorVisibilitySyncClient {
    private static final long LOCAL_MASK_RESEND_INTERVAL_TICKS = 40L;

    private ArmorVisibilitySyncClient() {
    }

    public static void onClientWorldLoad() {
        ArmorVisibilityClientCache.reset();
    }

    public static int getMask(Player player) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && player.getUUID().equals(client.player.getUUID())) {
            return ArmorSlotVisibilityState.getLocalMask();
        }

        return ArmorVisibilityClientCache.getSyncedMask(player.getUUID());
    }

    public static void onLocalConfigChanged() {
        ArmorVisibilityClientCache.markLocalStateDirty();

        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            ArmorVisibilityClientCache.setSyncedMask(client.player.getUUID(), ArmorSlotVisibilityState.getLocalMask());
        }
    }

    public static void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            ArmorVisibilityClientCache.reset();
            return;
        }

        ArmorVisibilityClientCache.advanceClientTick();
        syncLocalMask(client);
    }

    private static void syncLocalMask(Minecraft client) {
        int localMask = ArmorSlotVisibilityState.getLocalMask();
        boolean resendDue = ArmorVisibilityClientCache.lastSentTick() == Long.MIN_VALUE
                || ArmorVisibilityClientCache.currentClientTick() - ArmorVisibilityClientCache.lastSentTick() >= LOCAL_MASK_RESEND_INTERVAL_TICKS;
        if (!ArmorVisibilityClientCache.isLocalStateDirty()
                && localMask == ArmorVisibilityClientCache.lastSentMask()
                && !resendDue) {
            return;
        }

        ArmorVisibilityClientCache.setSyncedMask(client.player.getUUID(), localMask);

        RegistryFriendlyByteBuf buf = PlatformNetworking.createBuffer(client.level.registryAccess());
        new ArmorVisibilityUpdatePayload(localMask).write(buf);
        PlatformNetworking.sendToServer(ArmorVisibilityUpdatePayload.ID, buf);
        ArmorVisibilityClientCache.setLastSentMask(localMask);
        ArmorVisibilityClientCache.clearLocalStateDirty();
        ArmorVisibilityClientCache.setLastSentTick(ArmorVisibilityClientCache.currentClientTick());
    }

    public static void reset() {
        ArmorVisibilityClientCache.reset();
    }

    public static void handleSync(ArmorVisibilitySyncPayload payload) {
        ArmorVisibilityClientCache.setSyncedMask(payload.playerUuid(), payload.visibilityMask());
    }
}
