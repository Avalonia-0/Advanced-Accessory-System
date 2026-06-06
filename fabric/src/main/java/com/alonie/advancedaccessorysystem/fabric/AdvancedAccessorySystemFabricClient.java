package com.alonie.advancedaccessorysystem.fabric;

import com.alonie.advancedaccessorysystem.bootstrap.ClientBootstrap;
import net.fabricmc.api.ClientModInitializer;

public final class AdvancedAccessorySystemFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientBootstrap.register();
        // KeyMappings auto-register via Fabric API's KeyMapping constructor mixin in 1.21+
    }
}
