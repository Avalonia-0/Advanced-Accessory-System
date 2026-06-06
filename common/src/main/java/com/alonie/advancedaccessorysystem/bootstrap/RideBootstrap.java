package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.feature.ride.client.state.PassengerLaunchChargeState;
import com.alonie.advancedaccessorysystem.feature.ride.client.sync.RideStateSyncClient;
import com.alonie.advancedaccessorysystem.feature.ride.logic.PlayerHeadAccessoryBreakManager;
import com.alonie.advancedaccessorysystem.feature.ride.logic.PlayerRideInteractionHandler;
import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;

public final class RideBootstrap {
    private RideBootstrap() {
    }

    // Server tick registrations moved to PlatformLifecycle
    public static void registerCommon() {
        PlayerRideInteractionHandler.register();
        PlayerHeadAccessoryBreakManager.register();
    }

    public static void registerClient() {
        // Client tick registrations moved to PlatformLifecycle
    }
}
