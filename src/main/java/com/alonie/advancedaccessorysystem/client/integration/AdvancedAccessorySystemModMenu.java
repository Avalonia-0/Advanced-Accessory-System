package com.alonie.advancedaccessorysystem.client.integration;

import com.alonie.advancedaccessorysystem.client.gui.config.AdvancedAccessorySystemConfigGui;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class AdvancedAccessorySystemModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new AdvancedAccessorySystemConfigGui();
    }
}
