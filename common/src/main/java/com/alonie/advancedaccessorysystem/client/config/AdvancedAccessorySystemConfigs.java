package com.alonie.advancedaccessorysystem.client.config;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.function.BiFunction;

/**
 * Configuration facade.
 *
 * <p>When Cloth Config is present at runtime, values are persisted via
 * {@code AutoConfig} (JSON5 at {@code config/advanced_accessory_system.json5}).
 * When absent, falls back to the legacy manual Gson JSON at
 * {@code config/advanced-accessory-system.json}.
 *
 * <p>KeyMappings are defined here but must be registered with the
 * platform-specific keybinding registry during client bootstrap.
 */
public final class AdvancedAccessorySystemConfigs {

    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/Config");

    // ---- Config data (always available, with or without Cloth Config) -------

    public static boolean hideHelmet = false;
    public static boolean hideChestplate = false;
    public static boolean hideLeggings = false;
    public static boolean hideBoots = false;
    public static double boatPassengerAutoRideRadius = 2.0;
    public static double dismountPassengerLaunchSpeed = 0.8;
    public static double chargeIncreaseValue = 0.08;
    public static int chargeTime = 40;

    // ---- KeyMappings -------------------------------------------------------

    public static final KeyMapping openConfigHotkey = new KeyMapping(
            "key.advanced-accessory-system.openConfig",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, KeyMapping.Category.MISC);
    public static final KeyMapping openHeadShulkerHotkey = new KeyMapping(
            "key.advanced-accessory-system.openHeadShulker",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KeyMapping.Category.MISC);
    public static final KeyMapping dismountPassengersHotkey = new KeyMapping(
            "key.advanced-accessory-system.dismountPassengers",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Q, KeyMapping.Category.MISC);
    public static final KeyMapping chargePassengerLaunchHotkey = new KeyMapping(
            "key.advanced-accessory-system.chargePassengerLaunch",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, KeyMapping.Category.MISC);

    private static File configFile;
    private static Object clothHolder; // ConfigHolder<AccessoryConfig> if Cloth Config available
    private static boolean clothAvailable;
    private static boolean initialized;

    // ---- Init --------------------------------------------------------------

    public static void init() {
        if (initialized) return;
        initialized = true;

        clothAvailable = tryInitClothConfig();
        if (!clothAvailable) {
            initLegacy();
        }
    }

    private static boolean tryInitClothConfig() {
        try {
            Class<?> autoConfigClass = Class.forName("me.shedaniel.autoconfig.AutoConfig");
            Class<?> configClass = Class.forName(
                    "com.alonie.advancedaccessorysystem.client.config.AccessoryConfig");
            Class<?> serializerClass = Class.forName(
                    "me.shedaniel.autoconfig.serializer.GsonConfigSerializer");
            Class<?> configDefClass = Class.forName(
                    "me.shedaniel.autoconfig.util.ConfigDefinition");
            Class<?> factoryInterface = Class.forName(
                    "me.shedaniel.autoconfig.serializer.ConfigSerializer$Factory");

            // Create factory via Proxy to emulate GsonConfigSerializer::new method reference
            Constructor<?> gsonCtor = serializerClass.getConstructor(configDefClass);
            Object factory = Proxy.newProxyInstance(
                    factoryInterface.getClassLoader(),
                    new Class<?>[]{factoryInterface},
                    (_proxy, method, args) -> {
                        if ("create".equals(method.getName()) && args.length == 1) {
                            return gsonCtor.newInstance(args[0]);
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });

            // AutoConfig.register(AccessoryConfig.class, factory)
            var registerMethod = autoConfigClass.getMethod("register", Class.class, factoryInterface);
            clothHolder = registerMethod.invoke(null, configClass, factory);

            // Register save listener — sync Cloth Config GUI changes to static fields on every save
            var getHolderMethod = autoConfigClass.getMethod("getConfigHolder", Class.class);
            Object holder = getHolderMethod.invoke(null, configClass);
            var registerSaveListenerMethod = holder.getClass().getMethod("registerSaveListener",
                    BiFunction.class);
            BiFunction<Object, Object, Boolean> saveListener = (holder2, config) -> {
                syncFromConfig(config);
                return true;
            };
            registerSaveListenerMethod.invoke(holder, saveListener);

            // Initial sync from Cloth Config to static fields
            syncFromCloth();
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Cloth Config, falling back to legacy JSON", e);
            return false;
        }
    }

    private static void syncFromCloth() {
        if (clothHolder == null) return;
        try {
            Object cfg = clothHolder.getClass().getMethod("getConfig").invoke(clothHolder);
            syncFromConfig(cfg);
        } catch (Exception e) {
            LOGGER.warn("syncFromCloth failed: {}", e.getMessage());
        }
    }

    /** Sync static fields from a raw AccessoryConfig instance via reflection. */
    private static void syncFromConfig(Object cfg) {
        try {
            hideHelmet = cfg.getClass().getField("hideHelmet").getBoolean(cfg);
            hideChestplate = cfg.getClass().getField("hideChestplate").getBoolean(cfg);
            hideLeggings = cfg.getClass().getField("hideLeggings").getBoolean(cfg);
            hideBoots = cfg.getClass().getField("hideBoots").getBoolean(cfg);
            boatPassengerAutoRideRadius = cfg.getClass().getField("boatPassengerAutoRideRadius").getDouble(cfg);
            dismountPassengerLaunchSpeed = cfg.getClass().getField("dismountPassengerLaunchSpeed").getDouble(cfg);
            chargeIncreaseValue = cfg.getClass().getField("chargeIncreaseValue").getDouble(cfg);
            chargeTime = cfg.getClass().getField("chargeTime").getInt(cfg);
        } catch (Exception e) {
            LOGGER.warn("syncFromConfig failed: {}", e.getMessage());
        }
    }

    private static void saveCloth() {
        if (clothHolder == null) return;
        try {
            Object cfg = clothHolder.getClass().getMethod("getConfig").invoke(clothHolder);
            cfg.getClass().getField("hideHelmet").setBoolean(cfg, hideHelmet);
            cfg.getClass().getField("hideChestplate").setBoolean(cfg, hideChestplate);
            cfg.getClass().getField("hideLeggings").setBoolean(cfg, hideLeggings);
            cfg.getClass().getField("hideBoots").setBoolean(cfg, hideBoots);
            cfg.getClass().getField("boatPassengerAutoRideRadius").setDouble(cfg, boatPassengerAutoRideRadius);
            cfg.getClass().getField("dismountPassengerLaunchSpeed").setDouble(cfg, dismountPassengerLaunchSpeed);
            cfg.getClass().getField("chargeIncreaseValue").setDouble(cfg, chargeIncreaseValue);
            cfg.getClass().getField("chargeTime").setInt(cfg, chargeTime);
            clothHolder.getClass().getMethod("save").invoke(clothHolder);
        } catch (Exception e) {
            LOGGER.warn("saveCloth failed: {}", e.getMessage());
        }
    }

    // ---- Legacy fallback ---------------------------------------------------

    private static void initLegacy() {
        configFile = new File(Minecraft.getInstance().gameDirectory,
                "config/advanced-accessory-system.json");
        loadLegacy();
        saveLegacy();
    }

    private static void loadLegacy() {
        if (configFile == null || !configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            hideHelmet = getBool(json, "hideHelmet", false);
            hideChestplate = getBool(json, "hideChestplate", false);
            hideLeggings = getBool(json, "hideLeggings", false);
            hideBoots = getBool(json, "hideBoots", false);
            boatPassengerAutoRideRadius = getDouble(json, "boatPassengerAutoRideRadius", 2.0);
            dismountPassengerLaunchSpeed = getDouble(json, "dismountPassengerLaunchSpeed", 0.8);
            chargeIncreaseValue = getDouble(json, "chargeIncreaseValue", 0.08);
            chargeTime = getInt(json, "chargeTime", 40);
        } catch (IOException | JsonParseException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    private static void saveLegacy() {
        if (configFile == null) return;
        configFile.getParentFile().mkdirs();
        JsonObject json = new JsonObject();
        json.addProperty("hideHelmet", hideHelmet);
        json.addProperty("hideChestplate", hideChestplate);
        json.addProperty("hideLeggings", hideLeggings);
        json.addProperty("hideBoots", hideBoots);
        json.addProperty("boatPassengerAutoRideRadius", boatPassengerAutoRideRadius);
        json.addProperty("dismountPassengerLaunchSpeed", dismountPassengerLaunchSpeed);
        json.addProperty("chargeIncreaseValue", chargeIncreaseValue);
        json.addProperty("chargeTime", chargeTime);
        try (FileWriter writer = new FileWriter(configFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private static boolean getBool(JsonObject json, String key, boolean def) {
        return json.has(key) ? json.get(key).getAsBoolean() : def;
    }

    private static double getDouble(JsonObject json, String key, double def) {
        return json.has(key) ? json.get(key).getAsDouble() : def;
    }

    private static int getInt(JsonObject json, String key, int def) {
        return json.has(key) ? json.get(key).getAsInt() : def;
    }

    // ---- Public access -----------------------------------------------------

    public static boolean isLocalServerControlAvailable() {
        return Minecraft.getInstance().isLocalServer();
    }

    public static boolean isArmorHidden(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> hideHelmet;
            case CHEST -> hideChestplate;
            case LEGS -> hideLeggings;
            case FEET -> hideBoots;
            default -> false;
        };
    }

    public static double getBoatPassengerAutoRideRadius() { return boatPassengerAutoRideRadius; }
    public static double getDismountPassengerLaunchSpeed() { return dismountPassengerLaunchSpeed; }
    public static double getChargeIncreaseValue() { return chargeIncreaseValue; }
    public static int getChargeTime() { return chargeTime; }

    public static void applyChargeConfigValues(double increaseValue, int time) {
        chargeIncreaseValue = increaseValue;
        chargeTime = time;
        if (clothAvailable) saveCloth(); else saveLegacy();
    }

    public static boolean isClothConfigAvailable() {
        return clothAvailable;
    }

    /**
     * Open the Cloth Config GUI via reflection.
     * Safe to call when Cloth Config is absent — returns false.
     */
    public static boolean openConfigScreen() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return false;
        if (!clothAvailable) return false;
        try {
            Class<?> autoConfigClass = Class.forName("me.shedaniel.autoconfig.AutoConfig");
            Class<?> configClass = Class.forName(
                    "com.alonie.advancedaccessorysystem.client.config.AccessoryConfig");
            var getScreenMethod = autoConfigClass.getMethod("getConfigScreen",
                    Class.class, net.minecraft.client.gui.screens.Screen.class);
            @SuppressWarnings("unchecked")
            var supplier = (java.util.function.Supplier<net.minecraft.client.gui.screens.Screen>)
                    getScreenMethod.invoke(null, configClass, client.screen);
            client.setScreen(supplier.get());
            return true;
        } catch (Exception e) {
            LOGGER.warn("openConfigScreen failed: {}", e.getMessage());
            return false;
        }
    }
}
