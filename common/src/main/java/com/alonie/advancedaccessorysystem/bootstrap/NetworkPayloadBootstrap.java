package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.bridge.PlatformNetworking;

public final class NetworkPayloadBootstrap {
    private NetworkPayloadBootstrap() {
    }

    /** Register C2S network handlers (runs on both client and server). */
    public static void registerCommon() {
        PlatformNetworking.registerC2S();
    }
}
