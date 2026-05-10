package com.alonie.advancedaccessorysystem.client.gui.config;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import fi.dy.masa.malilib.config.gui.ButtonPressDirtyListenerSimple;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IConfigInfoProvider;
import fi.dy.masa.malilib.util.StringUtils;

import java.util.List;

public class AdvancedAccessorySystemConfigGui extends GuiConfigsBase {
    private static ConfigTab tab = ConfigTab.ARMOR_VISIBILITY;

    public AdvancedAccessorySystemConfigGui() {
        super(
                10,
                50,
                AdvancedAccessorySystemMod.MOD_ID,
                null,
                "advanced-accessory-system.title.configs"
        );
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        if (!tab.isAvailable()) {
            tab = ConfigTab.ARMOR_VISIBILITY;
        }

        int x = 10;
        int y = 26;

        for (ConfigTab configTab : ConfigTab.values()) {
            if (!configTab.isAvailable()) {
                continue;
            }

            ButtonGeneric button = new ButtonGeneric(x, y, -1, 20, configTab.getDisplayName());
            button.setEnabled(tab != configTab);
            this.addButton(button, new TabChangeListener(configTab, this));
            x += button.getWidth() + 2;
        }
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        return switch (tab) {
            case ARMOR_VISIBILITY -> ConfigOptionWrapper.createFor(AdvancedAccessorySystemConfigs.GENERIC);
            case PARAMETERS -> tab.isAvailable()
                    ? ConfigOptionWrapper.createFor(AdvancedAccessorySystemConfigs.PARAMETERS)
                    : List.of();
            case HOTKEYS -> ConfigOptionWrapper.createFor(AdvancedAccessorySystemConfigs.getVisibleHotkeys());
        };
    }

    @Override
    public String getModId() {
        return AdvancedAccessorySystemMod.MOD_ID;
    }

    @Override
    protected boolean useKeybindSearch() {
        return tab == ConfigTab.HOTKEYS;
    }

    @Override
    public ButtonPressDirtyListenerSimple getButtonPressListener() {
        return super.getButtonPressListener();
    }

    @Override
    public IConfigInfoProvider getHoverInfoProvider() {
        return super.getHoverInfoProvider();
    }

    private enum ConfigTab {
        ARMOR_VISIBILITY("advanced-accessory-system.config.tab.armor_visibility"),
        PARAMETERS("advanced-accessory-system.config.tab.parameters"),
        HOTKEYS("advanced-accessory-system.config.tab.hotkeys");

        private final String translationKey;

        ConfigTab(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }

        public boolean isAvailable() {
            if (this != PARAMETERS) {
                return true;
            }

            return AdvancedAccessorySystemConfigs.isLocalServerControlAvailable()
                    && !AdvancedAccessorySystemConfigs.isModpackModeEnabled();
        }
    }

    private record TabChangeListener(ConfigTab tab, AdvancedAccessorySystemConfigGui parent) implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            AdvancedAccessorySystemConfigGui.tab = this.tab;
            this.parent.reCreateListWidget();
            this.parent.getListWidget().resetScrollbarPosition();
            this.parent.initGui();
        }
    }
}
