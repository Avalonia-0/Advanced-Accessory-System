package com.alonie.advancedaccessorysystem.feature.ride.client.input;

import com.alonie.advancedaccessorysystem.collective.CollectiveNetworkingBridge;
import com.alonie.advancedaccessorysystem.feature.ride.client.state.PassengerLaunchChargeState;
import com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request.DismountPassengersPayload;
import net.minecraft.client.MinecraftClient;

public final class DismountPassengersClient {
    private DismountPassengersClient() {
    }

    public static boolean trigger() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return false;
        }

        CollectiveNetworkingBridge.sendToServer(new DismountPassengersPayload(
                PassengerLaunchChargeState.hasChargedLaunchOverride(),
                PassengerLaunchChargeState.getChargedLaunchSpeed()
        ));
        return true;
    }
}
