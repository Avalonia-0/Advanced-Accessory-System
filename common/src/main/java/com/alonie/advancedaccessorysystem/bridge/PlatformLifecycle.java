package com.alonie.advancedaccessorysystem.bridge;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.sync.ArmorVisibilitySyncManager;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionRegistry;
import com.alonie.advancedaccessorysystem.feature.ride.state.RideRuntimeSessionState;
import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

/**
 * Cross-platform lifecycle event bridge backed by Architectury API.
 * Server-side event registrations only.
 * Client-side registrations are in {@code PlatformLifecycleClient}.
 */
public final class PlatformLifecycle {
    private PlatformLifecycle() {
    }

    public static void registerCommon() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            ArmorVisibilitySyncManager.onPlayerLoggedIn(player);
            BoatPassengerSettingsSyncManager.onPlayerLoggedIn(player);
        });

        PlayerEvent.PLAYER_QUIT.register(player -> {
            ArmorVisibilitySyncManager.onPlayerLoggedOut(player);
        });

        LifecycleEvent.SERVER_STOPPING.register(server -> {
            ArmorVisibilitySyncManager.resetRuntimeState();
            BoatPassengerSettingsSyncManager.resetRuntimeState();
            RideRuntimeSessionState.reset();
            HeadShulkerSessionRegistry.reset();
        });

        TickEvent.SERVER_POST.register(server -> {
            ArmorVisibilitySyncManager.onServerTick(server);
            PlayerRideSyncManager.onServerTick(server);
        });
    }

    /** Convenience: register a custom server tick callback. */
    public static void registerServerTick(TickEvent.Server callback) {
        TickEvent.SERVER_POST.register(callback);
    }
}
