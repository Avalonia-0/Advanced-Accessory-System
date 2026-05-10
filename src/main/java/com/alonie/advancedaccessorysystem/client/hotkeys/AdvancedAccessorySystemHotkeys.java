package com.alonie.advancedaccessorysystem.client.hotkeys;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.client.gui.config.AdvancedAccessorySystemConfigGui;
import com.alonie.advancedaccessorysystem.feature.headshulker.client.input.OpenHeadShulkerClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.config.BoatPassengerWhitelistConfig;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatAutoPickUpRules;
import com.alonie.advancedaccessorysystem.feature.ride.client.input.DismountPassengersClient;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;

import java.util.List;

public final class AdvancedAccessorySystemHotkeys {
    private static final String BROADCAST_SENDER = "Advanced Accessory System";
    private static final String CATEGORY_NAME = "Advanced Accessory System";
    private static final String CATEGORY_TRANSLATION_KEY = "advanced-accessory-system.hotkeys.category";
    private static final List<IHotkey> HOTKEYS = List.of(
            AdvancedAccessorySystemConfigs.OPEN_HEAD_SHULKER_HOTKEY,
            AdvancedAccessorySystemConfigs.OPEN_CONFIG_GUI_HOTKEY,
            AdvancedAccessorySystemConfigs.TOGGLE_TARGET_ENTITY_IN_BOAT_AUTO_PICK_UP_HOTKEY,
            AdvancedAccessorySystemConfigs.DISMOUNT_PASSENGERS_HOTKEY,
            AdvancedAccessorySystemConfigs.CHARGE_PASSENGER_LAUNCH_HOTKEY
    );
    private static final IKeybindProvider PROVIDER = new KeybindProvider();

    private AdvancedAccessorySystemHotkeys() {
    }

    public static void init() {
        AdvancedAccessorySystemConfigs.OPEN_HEAD_SHULKER_HOTKEY.getKeybind().setCallback(new OpenHeadShulkerCallback());
        AdvancedAccessorySystemConfigs.OPEN_CONFIG_GUI_HOTKEY.getKeybind().setCallback(new OpenConfigGuiCallback());
        AdvancedAccessorySystemConfigs.TOGGLE_TARGET_ENTITY_IN_BOAT_AUTO_PICK_UP_HOTKEY.getKeybind()
                .setCallback(new ToggleTargetEntityInBoatAutoPickUpCallback());
        AdvancedAccessorySystemConfigs.DISMOUNT_PASSENGERS_HOTKEY.getKeybind()
                .setCallback(new DismountPassengersCallback());
        InputEventHandler.getKeybindManager().registerKeybindProvider(PROVIDER);
        InputEventHandler.getKeybindManager().updateUsedKeys();
    }

    private static class KeybindProvider implements IKeybindProvider {
        @Override
        public void addKeysToMap(IKeybindManager manager) {
            for (IHotkey hotkey : HOTKEYS) {
                manager.addKeybindToMap(hotkey.getKeybind());
            }
        }

        @Override
        public void addHotkeys(IKeybindManager manager) {
            manager.addHotkeysForCategory(CATEGORY_NAME, StringUtils.translate(CATEGORY_TRANSLATION_KEY), HOTKEYS);
        }
    }

    private static class OpenHeadShulkerCallback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (action != KeyAction.PRESS) {
                return false;
            }

            return OpenHeadShulkerClient.trigger();
        }
    }

    private static class OpenConfigGuiCallback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (action != KeyAction.PRESS) {
                return false;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return false;
            }

            GuiBase.openGui(new AdvancedAccessorySystemConfigGui());
            return true;
        }
    }

    private static class ToggleTargetEntityInBoatAutoPickUpCallback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (action != KeyAction.PRESS) {
                return false;
            }

            if (!AdvancedAccessorySystemConfigs.isLocalServerControlAvailable()) {
                return false;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null || !(client.crosshairTarget instanceof EntityHitResult hitResult)) {
                return false;
            }

            Identifier entityTypeId = Registries.ENTITY_TYPE.getId(hitResult.getEntity().getType());
            BoatAutoPickUpRules.ToggleResult result = BoatPassengerWhitelistConfig.toggleEntityTypeId(entityTypeId);
            if (result == BoatAutoPickUpRules.ToggleResult.NONE) {
                return false;
            }

            broadcastWhitelistUpdate(client, entityTypeId, result == BoatAutoPickUpRules.ToggleResult.ADDED);
            return true;
        }
    }

    private static class DismountPassengersCallback implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (action != KeyAction.PRESS) {
                return false;
            }

            return DismountPassengersClient.trigger();
        }
    }

    private static void broadcastWhitelistUpdate(MinecraftClient client, Identifier entityTypeId, boolean added) {
        if (client.player == null || client.getServer() == null) {
            return;
        }

        Text content = Text.translatable(
                added
                        ? "advanced-accessory-system.message.boatAutoPickUpAdded"
                        : "advanced-accessory-system.message.boatAutoPickUpRemoved",
                entityTypeId.toString()
        );
        Text message = Text.translatable("chat.type.text", Text.literal(BROADCAST_SENDER), content);
        client.getServer().execute(() -> client.getServer().getPlayerManager().broadcast(message, false));
    }
}
