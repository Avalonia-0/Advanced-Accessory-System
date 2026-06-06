package com.alonie.advancedaccessorysystem.feature.ride.client.input;

import com.alonie.advancedaccessorysystem.bridge.PlatformNetworking;
import com.alonie.advancedaccessorysystem.feature.ride.client.state.PassengerLaunchChargeState;
import com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request.DismountPassengersPayload;
import net.minecraft.client.Minecraft;

public final class DismountPassengersClient {
    private DismountPassengersClient() {
    }

    public static boolean trigger() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return false;
        }

        var buf = PlatformNetworking.createBuffer(Minecraft.getInstance().level.registryAccess());
        new DismountPassengersPayload(
                PassengerLaunchChargeState.hasChargedLaunchOverride(),
                PassengerLaunchChargeState.getChargedLaunchSpeed()
        ).write(buf);
        PlatformNetworking.sendToServer(DismountPassengersPayload.ID, buf);
        return true;
    }
}
