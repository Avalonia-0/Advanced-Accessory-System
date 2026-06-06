package com.alonie.advancedaccessorysystem.fabric;

import com.alonie.advancedaccessorysystem.bootstrap.CommonBootstrap;
import com.alonie.advancedaccessorysystem.compat.trinkets.TrinketsCompatInitializer;
import net.fabricmc.api.ModInitializer;

public final class AdvancedAccessorySystemFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonBootstrap.register();
        TrinketsCompatInitializer.init();
    }
}
