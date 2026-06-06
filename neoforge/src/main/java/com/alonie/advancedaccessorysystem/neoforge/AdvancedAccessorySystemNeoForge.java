package com.alonie.advancedaccessorysystem.neoforge;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.bootstrap.CommonBootstrap;
import com.alonie.advancedaccessorysystem.compat.trinkets.TrinketsCompatInitializer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(AdvancedAccessorySystemMod.MOD_ID)
public final class AdvancedAccessorySystemNeoForge {
    public AdvancedAccessorySystemNeoForge() {
        CommonBootstrap.register();
        TrinketsCompatInitializer.init();

        // Register Cloth Config GUI in NeoForge's Mods screen (via reflection,
        // safe even if Cloth Config jar isn't loaded as a NeoForge mod)
        try {
            Class<?> configClass = Class.forName(
                    "com.alonie.advancedaccessorysystem.client.config.AccessoryConfig");
            Class<?> autoConfigClientClass = Class.forName(
                    "me.shedaniel.autoconfig.AutoConfigClient");
            var getScreenMethod = autoConfigClientClass.getMethod(
                    "getConfigScreen", Class.class, net.minecraft.client.gui.screens.Screen.class);

            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> (container, parent) -> {
                        try {
                            @SuppressWarnings("unchecked")
                            var supplier = (java.util.function.Supplier<net.minecraft.client.gui.screens.Screen>)
                                    getScreenMethod.invoke(null, configClass, parent);
                            return supplier.get();
                        } catch (Exception e) {
                            return null;
                        }
                    }
            );
        } catch (Exception e) {
            // Cloth Config not available — no config GUI in Mods screen
        }
    }
}
