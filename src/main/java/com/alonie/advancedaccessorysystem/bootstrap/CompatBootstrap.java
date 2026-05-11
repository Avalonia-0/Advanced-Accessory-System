package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.compat.CompatServices;

public final class CompatBootstrap {
    private CompatBootstrap() {
    }

    public static void registerCommon() {
        CompatServices.headAccessoryBridge().initCommon();
    }

    public static void registerClient() {
        CompatServices.headAccessoryBridge().initClient();
    }
}
