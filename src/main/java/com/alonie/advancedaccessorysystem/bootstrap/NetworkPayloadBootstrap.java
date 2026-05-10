package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.collective.CollectiveNetworkingBridge;

public final class NetworkPayloadBootstrap {
    private NetworkPayloadBootstrap() {
    }

    public static void register() {
        CollectiveNetworkingBridge.register();
    }
}
