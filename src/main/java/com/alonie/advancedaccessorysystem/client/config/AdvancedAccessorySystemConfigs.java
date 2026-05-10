package com.alonie.advancedaccessorysystem.client.config;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.client.gui.config.AdvancedAccessorySystemConfigGui;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.config.BoatPassengerWhitelistConfig;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.ChargeConfigData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.EquipmentSlot;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class AdvancedAccessorySystemConfigs implements IConfigHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String MODPACK_KEY = "modpack";
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(AdvancedAccessorySystemMod.MOD_ID + ".json");
    private static boolean modpackModeEnabled;

    public static final ConfigBoolean HIDE_HEAD_ARMOR = new ConfigBoolean(
            "hideHeadArmor",
            false,
            "Hide the rendered helmet model and its equipped render-state layers without removing equipment attributes."
    ).apply("advanced-accessory-system.config");

    public static final ConfigBoolean HIDE_CHEST_ARMOR = new ConfigBoolean(
            "hideChestArmor",
            false,
            "Hide the rendered chest armor model and its equipped render-state layers without removing equipment attributes."
    ).apply("advanced-accessory-system.config");

    public static final ConfigBoolean HIDE_LEGS_ARMOR = new ConfigBoolean(
            "hideLegsArmor",
            false,
            "Hide the rendered leggings model and its equipped render-state layers without removing equipment attributes."
    ).apply("advanced-accessory-system.config");

    public static final ConfigBoolean HIDE_FEET_ARMOR = new ConfigBoolean(
            "hideFeetArmor",
            false,
            "Hide the rendered boots model and its equipped render-state layers without removing equipment attributes."
    ).apply("advanced-accessory-system.config");

    public static final List<IConfigBase> GENERIC = List.of(
            HIDE_HEAD_ARMOR,
            HIDE_CHEST_ARMOR,
            HIDE_LEGS_ARMOR,
            HIDE_FEET_ARMOR
    );

    public static final ConfigDouble BOAT_PASSENGER_AUTO_RIDE_RADIUS = new ConfigDouble(
            "boatPassengerAutoRideRadius",
            BoatPassengerConfigHelper.DEFAULT_AUTO_RIDE_RADIUS,
            0.0D,
            128.0D,
            false,
            "When a head item matched by AAS_boat is worn, matching nearby entities within this radius from AAS_boat_auto_pick_up.allowed will be automatically picked up by the player."
    ).apply("advanced-accessory-system.config");

    public static final ConfigDouble DISMOUNT_PASSENGER_LAUNCH_SPEED = new ConfigDouble(
            "dismountPassengerLaunchSpeed",
            BoatPassengerConfigHelper.DEFAULT_DISMOUNT_LAUNCH_SPEED,
            BoatPassengerConfigHelper.MIN_DISMOUNT_LAUNCH_SPEED,
            BoatPassengerConfigHelper.MAX_DISMOUNT_LAUNCH_SPEED,
            false,
            "When ejecting your passengers from the hotkey, apply this launch acceleration magnitude in the vehicle player's facing direction."
    ).apply("advanced-accessory-system.config");

    public static final ConfigDouble CHARGE_INCREASE_VALUE = new ConfigDouble(
            "chargeIncreaseValue",
            BoatPassengerConfigHelper.DEFAULT_CHARGE_INCREASE_VALUE,
            0.0D,
            BoatPassengerConfigHelper.MAX_DISMOUNT_LAUNCH_SPEED,
            false,
            "Increase the accumulated launch acceleration magnitude by this value each tick while charging."
    ).apply("advanced-accessory-system.config");

    public static final ConfigInteger CHARGE_TIME = new ConfigInteger(
            "chargeTime",
            BoatPassengerConfigHelper.DEFAULT_CHARGE_TIME,
            0,
            72000,
            false,
            "Maximum charge duration in ticks."
    ).apply("advanced-accessory-system.config");

    public static final List<IConfigBase> PARAMETERS = List.of(
            BOAT_PASSENGER_AUTO_RIDE_RADIUS,
            DISMOUNT_PASSENGER_LAUNCH_SPEED,
            CHARGE_INCREASE_VALUE,
            CHARGE_TIME
    );

    private static final List<IConfigBase> PERSISTED_PARAMETERS = List.of(
            BOAT_PASSENGER_AUTO_RIDE_RADIUS,
            DISMOUNT_PASSENGER_LAUNCH_SPEED
    );

    public static final ConfigHotkey OPEN_HEAD_SHULKER_HOTKEY = new ConfigHotkey(
            "openHeadShulkerHotkey",
            "B",
            "Press this hotkey to open the shulker box on your head."
    ).apply("advanced-accessory-system.config");

    public static final ConfigHotkey OPEN_CONFIG_GUI_HOTKEY = new ConfigHotkey(
            "openConfigGuiHotkey",
            "Z,C",
            "Open the Advanced Accessory System malilib config screen."
    ).apply("advanced-accessory-system.config");

    public static final ConfigHotkey TOGGLE_TARGET_ENTITY_IN_BOAT_AUTO_PICK_UP_HOTKEY = new ConfigHotkey(
            "toggleTargetEntityInBoatAutoPickUpHotkey",
            "",
            "Press this hotkey to add or remove the entity under your crosshair to the AAS boat auto-pickup list. The config file is located at /config/advanced-accessory-system.json."
    ).apply("advanced-accessory-system.config");

    public static final ConfigHotkey DISMOUNT_PASSENGERS_HOTKEY = new ConfigHotkey(
            "dismountPassengersHotkey",
            "LEFT_SHIFT,Q",
            "Press this hotkey to eject your own passengers."
    ).apply("advanced-accessory-system.config");

    public static final ConfigHotkey CHARGE_PASSENGER_LAUNCH_HOTKEY = new ConfigHotkey(
            "chargePassengerLaunchHotkey",
            "LEFT_SHIFT",
            "Hold this hotkey to charge launch acceleration magnitude for supported passengers."
    ).apply("advanced-accessory-system.config");

    public static final List<IConfigBase> HOTKEYS = List.of(
            OPEN_HEAD_SHULKER_HOTKEY,
            OPEN_CONFIG_GUI_HOTKEY,
            TOGGLE_TARGET_ENTITY_IN_BOAT_AUTO_PICK_UP_HOTKEY,
            DISMOUNT_PASSENGERS_HOTKEY,
            CHARGE_PASSENGER_LAUNCH_HOTKEY
    );

    private static final AdvancedAccessorySystemConfigs INSTANCE = new AdvancedAccessorySystemConfigs();

    private AdvancedAccessorySystemConfigs() {
    }

    public static void init() {
        ConfigManager.getInstance().registerConfigHandler(AdvancedAccessorySystemMod.MOD_ID, INSTANCE);
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(
                        AdvancedAccessorySystemMod.MOD_ID,
                        I18n.hasTranslation("advanced-accessory-system.name")
                                ? I18n.translate("advanced-accessory-system.name")
                                : "Advanced Accessory System",
                        AdvancedAccessorySystemConfigGui::new
                )
        );
        INSTANCE.load();
        BoatPassengerWhitelistConfig.init();
    }

    public static boolean isArmorHidden(EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD) {
            return HIDE_HEAD_ARMOR.getBooleanValue();
        }
        if (slot == EquipmentSlot.CHEST) {
            return HIDE_CHEST_ARMOR.getBooleanValue();
        }
        if (slot == EquipmentSlot.LEGS) {
            return HIDE_LEGS_ARMOR.getBooleanValue();
        }
        if (slot == EquipmentSlot.FEET) {
            return HIDE_FEET_ARMOR.getBooleanValue();
        }

        return false;
    }

    public static double getBoatPassengerAutoRideRadius() {
        return BOAT_PASSENGER_AUTO_RIDE_RADIUS.getDoubleValue();
    }

    public static double getDismountPassengerLaunchSpeed() {
        return DISMOUNT_PASSENGER_LAUNCH_SPEED.getDoubleValue();
    }

    public static double getChargeIncreaseValue() {
        return CHARGE_INCREASE_VALUE.getDoubleValue();
    }

    public static int getChargeTime() {
        return CHARGE_TIME.getIntegerValue();
    }

    public static void applyChargeConfigValues(ChargeConfigData chargeConfig) {
        ChargeConfigData effectiveConfig = chargeConfig == null ? ChargeConfigData.DEFAULT : chargeConfig;
        CHARGE_INCREASE_VALUE.setDoubleValue(effectiveConfig.increaseValue());
        CHARGE_TIME.setIntegerValue(effectiveConfig.chargeTime());
    }

    public static boolean isLocalServerControlAvailable() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.isIntegratedServerRunning();
    }

    public static boolean isModpackModeEnabled() {
        return modpackModeEnabled;
    }

    public static List<IConfigBase> getVisibleHotkeys() {
        if (isLocalServerControlAvailable()) {
            return HOTKEYS;
        }

        return List.of(
                OPEN_HEAD_SHULKER_HOTKEY,
                OPEN_CONFIG_GUI_HOTKEY,
                DISMOUNT_PASSENGERS_HOTKEY,
                CHARGE_PASSENGER_LAUNCH_HOTKEY
        );
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }

    @Override
    public void load() {
        if (!Files.exists(CONFIG_PATH)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!(element instanceof JsonObject jsonObject)) {
                return;
            }

            modpackModeEnabled = readBoolean(jsonObject, MODPACK_KEY, false);
            ConfigUtils.readConfigBase(jsonObject, "generic", GENERIC);
            ConfigUtils.readConfigBase(jsonObject, "parameters", PERSISTED_PARAMETERS);
            ConfigUtils.readConfigBase(jsonObject, "hotkeys", HOTKEYS);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void save() {
        syncRuntimeBackedConfigValues();

        JsonObject root = new JsonObject();
        root.addProperty(MODPACK_KEY, modpackModeEnabled);
        ConfigUtils.writeConfigBase(root, "generic", GENERIC);
        ConfigUtils.writeConfigBase(root, "parameters", PERSISTED_PARAMETERS);
        ConfigUtils.writeConfigBase(root, "hotkeys", HOTKEYS);
        BoatPassengerWhitelistConfig.writeToConfigRoot(root);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onConfigsChanged() {
        syncRuntimeBackedConfigValues();
        this.save();
        ArmorVisibilitySyncClient.onLocalConfigChanged();
        BoatPassengerSettingsSyncClient.onLocalConfigChanged();
    }

    private static void syncRuntimeBackedConfigValues() {
        BoatPassengerWhitelistConfig.updateChargeConfigValues(
                getChargeIncreaseValue(),
                getChargeTime()
        );
    }

    private static boolean readBoolean(JsonObject jsonObject, String key, boolean fallback) {
        JsonElement element = jsonObject.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
                ? element.getAsBoolean()
                : fallback;
    }
}
