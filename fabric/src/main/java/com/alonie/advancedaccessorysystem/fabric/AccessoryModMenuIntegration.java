package com.alonie.advancedaccessorysystem.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

/**
 * ModMenu integration — adds an "Advanced Accessory System" entry
 * to the ModMenu mod list that opens the Cloth Config GUI.
 *
 * <p>Uses reflection for Cloth Config access so this class loads
 * correctly even when Cloth Config is absent from the runtime classpath.
 */
public class AccessoryModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(parent);
    }

    private static Screen createConfigScreen(Screen parent) {
        try {
            Class<?> autoConfigClass = Class.forName("me.shedaniel.autoconfig.AutoConfig");
            Class<?> configClass = Class.forName(
                    "com.alonie.advancedaccessorysystem.client.config.AccessoryConfig");
            var method = autoConfigClass.getMethod("getConfigScreen", Class.class, Screen.class);
            @SuppressWarnings("unchecked")
            var supplier = (java.util.function.Supplier<Screen>) method.invoke(null, configClass, parent);
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }
}
