package com.alonie.advancedaccessorysystem.bridge;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync.ArmorVisibilitySyncPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.s2c.sync.BoatPassengerSettingsSyncPayload;
import com.alonie.advancedaccessorysystem.feature.ride.client.sync.RideStateSyncClient;
import com.alonie.advancedaccessorysystem.feature.ride.network.s2c.sync.RideStateSyncPayload;
import dev.architectury.networking.NetworkManager;

/**
 * Client-side S2C (server→client) packet registration.
 * Only loaded on the client.
 */
public final class PlatformNetworkingClient {
    private PlatformNetworkingClient() {
    }

    /** Register S2C packet handlers (only fires on the client). */
    public static void registerS2C() {
        // -- S2C: Armor Visibility Sync --
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ArmorVisibilitySyncPayload.ID,
                (buf, ctx) -> {
                    ArmorVisibilitySyncPayload p = ArmorVisibilitySyncPayload.read(buf);
                    ctx.queue(() -> ArmorVisibilitySyncClient.handleSync(p));
                });

        // -- S2C: Boat Passenger Settings Sync --
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BoatPassengerSettingsSyncPayload.ID,
                (buf, ctx) -> {
                    BoatPassengerSettingsSyncPayload p = BoatPassengerSettingsSyncPayload.read(buf);
                    ctx.queue(() -> BoatPassengerSettingsSyncClient.handleSync(p));
                });

        // -- S2C: Ride State Sync --
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, RideStateSyncPayload.ID,
                (buf, ctx) -> {
                    RideStateSyncPayload p = RideStateSyncPayload.read(buf);
                    ctx.queue(() -> RideStateSyncClient.handleSync(p));
                });
    }
}
