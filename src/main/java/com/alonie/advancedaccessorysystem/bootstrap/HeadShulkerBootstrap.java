package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.feature.headshulker.logic.CosmeticHeadShulkerBridge;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class HeadShulkerBootstrap {
    private HeadShulkerBootstrap() {
    }

    public static void registerCommon() {
        ServerTickEvents.END_SERVER_TICK.register(server -> CosmeticHeadShulkerBridge.tickAllSessions());
    }
}
