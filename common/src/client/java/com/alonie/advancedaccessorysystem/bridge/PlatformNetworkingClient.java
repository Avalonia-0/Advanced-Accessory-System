package com.alonie.advancedaccessorysystem.bridge;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync.ArmorVisibilitySyncPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.s2c.sync.BoatPassengerSettingsSyncPayload;
import dev.architectury.networking.NetworkManager;

public final class PlatformNetworkingClient {
    private PlatformNetworkingClient() {
    }

    public static void registerS2C() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ArmorVisibilitySyncPayload.ID,
                (buf, ctx) -> {
                    ArmorVisibilitySyncPayload p = ArmorVisibilitySyncPayload.read(buf);
                    ctx.queue(() -> ArmorVisibilitySyncClient.handleSync(p));
                });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BoatPassengerSettingsSyncPayload.ID,
                (buf, ctx) -> {
                    BoatPassengerSettingsSyncPayload p = BoatPassengerSettingsSyncPayload.read(buf);
                    ctx.queue(() -> BoatPassengerSettingsSyncClient.handleSync(p));
                });
    }
}
