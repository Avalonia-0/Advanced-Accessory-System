package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.feature.headslot.rule.AllItemsHeadEquippablePatch;

public final class HeadSlotBootstrap {
    private HeadSlotBootstrap() {
    }

    public static void registerCommon() {
        AllItemsHeadEquippablePatch.register();
    }
}
