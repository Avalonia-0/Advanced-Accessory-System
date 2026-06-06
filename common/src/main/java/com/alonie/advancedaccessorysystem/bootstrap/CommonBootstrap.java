package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.bridge.PlatformLifecycle;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.TrinketsHatSlotProvider;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.VanillaHeadSlotProvider;

import java.util.concurrent.atomic.AtomicBoolean;

public final class CommonBootstrap {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private CommonBootstrap() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) return;
        // Accessory slot providers — registered first so features can query them.
        AccessorySlotRegistry.register(new VanillaHeadSlotProvider());
        AccessorySlotRegistry.register(new TrinketsHatSlotProvider());

        NetworkPayloadBootstrap.register();
        PlatformLifecycle.registerCommon();
        HeadSlotBootstrap.registerCommon();
        ArmorVisibilityBootstrap.registerCommon();
        BoatPassengerBootstrap.registerCommon();
        RideBootstrap.registerCommon();
        HeadShulkerBootstrap.registerCommon();
    }
}
