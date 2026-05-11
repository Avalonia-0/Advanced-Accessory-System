package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.feature.ride.client.state.PassengerLaunchChargeState;
import com.alonie.advancedaccessorysystem.feature.ride.client.sync.RideStateSyncClient;
import com.alonie.advancedaccessorysystem.feature.ride.logic.PlayerHeadAccessoryBreakManager;
import com.alonie.advancedaccessorysystem.feature.ride.logic.PlayerRideInteractionHandler;
import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;

public final class RideBootstrap {
    private RideBootstrap() {
    }

    public static void registerCommon() {
        PlayerRideInteractionHandler.register();
        PlayerRideSyncManager.register();
        PlayerHeadAccessoryBreakManager.register();
    }

    public static void registerClient() {
        PassengerLaunchChargeState.register();
        RideStateSyncClient.register();
    }
}
