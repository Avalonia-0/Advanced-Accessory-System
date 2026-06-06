package com.alonie.advancedaccessorysystem.fabric;

import com.alonie.advancedaccessorysystem.bootstrap.ClientBootstrap;
import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class AdvancedAccessorySystemFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientBootstrap.register();

        // Explicit keybinding registration for Fabric (compatible across Fabric API versions)
        KeyBindingHelper.registerKeyBinding(AdvancedAccessorySystemConfigs.openConfigHotkey);
        KeyBindingHelper.registerKeyBinding(AdvancedAccessorySystemConfigs.openHeadShulkerHotkey);
        KeyBindingHelper.registerKeyBinding(AdvancedAccessorySystemConfigs.dismountPassengersHotkey);
        KeyBindingHelper.registerKeyBinding(AdvancedAccessorySystemConfigs.chargePassengerLaunchHotkey);
    }
}
