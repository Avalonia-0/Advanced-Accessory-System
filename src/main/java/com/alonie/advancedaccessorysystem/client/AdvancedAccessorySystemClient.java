package com.alonie.advancedaccessorysystem.client;

import com.alonie.advancedaccessorysystem.bootstrap.ClientBootstrap;
import net.fabricmc.api.ClientModInitializer;

public final class AdvancedAccessorySystemClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientBootstrap.register();
    }
}
