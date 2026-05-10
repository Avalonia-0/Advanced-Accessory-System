package com.alonie.advancedaccessorysystem.collective;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.sync.ArmorVisibilitySyncManager;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionRegistry;
import com.alonie.advancedaccessorysystem.feature.ride.client.sync.RideStateSyncClient;
import com.alonie.advancedaccessorysystem.feature.ride.state.RideRuntimeSessionState;
import com.natamus.collective.fabric.callbacks.CollectiveClientEvents;
import com.natamus.collective.fabric.callbacks.CollectivePlayerEvents;
import com.natamus.collective.fabric.callbacks.CollectiveWorldEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CollectiveLifecycleBridge {
    private CollectiveLifecycleBridge() {
    }

    public static void registerCommon() {
        CollectivePlayerEvents.PLAYER_LOGGED_IN.register((world, player) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                CollectiveTaskBridge.runServer(serverPlayer, () -> {
                    ArmorVisibilitySyncManager.onPlayerLoggedIn(serverPlayer);
                    BoatPassengerSettingsSyncManager.onPlayerLoggedIn(serverPlayer);
                });
            }
        });

        CollectivePlayerEvents.PLAYER_LOGGED_OUT.register((world, player) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                CollectiveTaskBridge.runServer(serverPlayer, () ->
                        ArmorVisibilitySyncManager.onPlayerLoggedOut(serverPlayer));
            }
        });

        CollectiveWorldEvents.WORLD_UNLOAD.register(world -> {
            ArmorVisibilitySyncManager.resetRuntimeState();
            BoatPassengerSettingsSyncManager.resetRuntimeState();
            RideRuntimeSessionState.reset();
            HeadShulkerSessionRegistry.reset();
        });
    }

    public static void registerClient() {
        CollectiveClientEvents.CLIENT_WORLD_LOAD.register(world -> {
            ArmorVisibilitySyncClient.onClientWorldLoad();
            BoatPassengerSettingsSyncClient.onClientWorldLoad();
            RideStateSyncClient.onClientWorldLoad();
        });
    }
}
