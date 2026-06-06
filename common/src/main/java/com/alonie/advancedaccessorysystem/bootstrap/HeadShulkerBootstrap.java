package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.bridge.PlatformLifecycle;
import com.alonie.advancedaccessorysystem.feature.headshulker.logic.CosmeticHeadShulkerBridge;

public final class HeadShulkerBootstrap {
    private HeadShulkerBootstrap() {
    }

    public static void registerCommon() {
        PlatformLifecycle.registerServerTick(server -> CosmeticHeadShulkerBridge.tickAllSessions());
    }
}
