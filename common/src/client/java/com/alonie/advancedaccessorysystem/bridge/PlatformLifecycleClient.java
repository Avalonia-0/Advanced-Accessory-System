package com.alonie.advancedaccessorysystem.bridge;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.headshulker.client.input.OpenHeadShulkerClient;
import com.alonie.advancedaccessorysystem.feature.ride.client.input.DismountPassengersClient;
import com.alonie.advancedaccessorysystem.feature.ride.client.state.PassengerLaunchChargeState;
import com.alonie.advancedaccessorysystem.feature.ride.client.sync.RideStateSyncClient;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;

/**
 * Client-side lifecycle event bridge backed by Architectury API.
 * Only loaded on the client; all registrations here are client-only.
 */
public final class PlatformLifecycleClient {
    private PlatformLifecycleClient() {
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

    /** Convenience: register a custom client tick callback. */
    public static void registerClientTick(ClientTickEvent.Client callback) {
        ClientTickEvent.CLIENT_POST.register(callback);
    }
}
