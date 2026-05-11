package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.sync.ArmorVisibilitySyncManager;

public final class ArmorVisibilityBootstrap {
    private ArmorVisibilityBootstrap() {
    }

    public static void registerCommon() {
        ArmorVisibilitySyncManager.register();
    }

    public static void registerClient() {
        ArmorVisibilitySyncClient.register();
    }
}
