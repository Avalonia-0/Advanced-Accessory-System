package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.bridge.PlatformNetworking;

public final class NetworkPayloadBootstrap {
    private NetworkPayloadBootstrap() {
    }

    public static void register() {
        PlatformNetworking.register();
    }
}
