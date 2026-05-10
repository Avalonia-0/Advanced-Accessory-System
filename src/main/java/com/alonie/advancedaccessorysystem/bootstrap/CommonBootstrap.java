package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.collective.CollectiveLifecycleBridge;

public final class CommonBootstrap {
    private CommonBootstrap() {
    }

    public static void register() {
        NetworkPayloadBootstrap.register();
        CollectiveLifecycleBridge.registerCommon();
        HeadSlotBootstrap.registerCommon();
        CompatBootstrap.registerCommon();
        ArmorVisibilityBootstrap.registerCommon();
        BoatPassengerBootstrap.registerCommon();
        RideBootstrap.registerCommon();
        HeadShulkerBootstrap.registerCommon();
    }
}
