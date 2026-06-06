package com.alonie.advancedaccessorysystem.bridge;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.sync.ArmorVisibilitySyncManager;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;
import com.alonie.advancedaccessorysystem.feature.headshulker.client.input.OpenHeadShulkerClient;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionRegistry;
import com.alonie.advancedaccessorysystem.feature.ride.client.input.DismountPassengersClient;
import com.alonie.advancedaccessorysystem.feature.ride.client.state.PassengerLaunchChargeState;
import com.alonie.advancedaccessorysystem.feature.ride.client.sync.RideStateSyncClient;
import com.alonie.advancedaccessorysystem.feature.ride.state.RideRuntimeSessionState;
import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

/**
 * Cross-platform lifecycle event bridge backed by Architectury API.
 * No Fabric API references.
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

    public static void registerClient() {
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(level -> {
            ArmorVisibilitySyncClient.onClientWorldLoad();
            BoatPassengerSettingsSyncClient.onClientWorldLoad();
            RideStateSyncClient.onClientWorldLoad();
        });

        ClientTickEvent.CLIENT_POST.register(client -> {
            ArmorVisibilitySyncClient.onClientTick(client);
            BoatPassengerSettingsSyncClient.onClientTick(client);
            PassengerLaunchChargeState.onClientTick(client);
            RideStateSyncClient.onClientTick(client);

            // Keybinding handlers
            if (AdvancedAccessorySystemConfigs.openConfigHotkey.consumeClick()) {
                AdvancedAccessorySystemConfigs.openConfigScreen();
            }
            if (AdvancedAccessorySystemConfigs.openHeadShulkerHotkey.consumeClick()) {
                OpenHeadShulkerClient.trigger();
            }
            if (AdvancedAccessorySystemConfigs.dismountPassengersHotkey.consumeClick()) {
                DismountPassengersClient.trigger();
            }
        });

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            BoatPassengerSettingsSyncClient.resetAllState();
            RideStateSyncClient.clear();
        });
    }

    // Convenience: register a custom server tick callback
    public static void registerServerTick(TickEvent.Server callback) {
        TickEvent.SERVER_POST.register(callback);
    }

    // Convenience: register a custom client tick callback
    public static void registerClientTick(ClientTickEvent.Client callback) {
        ClientTickEvent.CLIENT_POST.register(callback);
    }
}
