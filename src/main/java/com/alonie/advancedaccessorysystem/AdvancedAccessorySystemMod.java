package com.alonie.advancedaccessorysystem;

import com.alonie.advancedaccessorysystem.bootstrap.CommonBootstrap;
import net.fabricmc.api.ModInitializer;

public final class AdvancedAccessorySystemMod implements ModInitializer {
    public static final String MOD_ID = "advanced-accessory-system";

    @Override
    public void onInitialize() {
        CommonBootstrap.register();
    }
}
