package com.alonie.advancedaccessorysystem.feature.boatpassenger.client.config;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.accessory.state.AccessoryPatternRuntimeState;
import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatAutoPickUpRules;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.ChargeConfigData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

public final class BoatPassengerWhitelistConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final String AUTO_PICK_UP_KEY = "AAS_boat_auto_pick_up";
    private static final String CHARGE_KEY = "charge";
    private static final String LEGACY_ENTITY_IDS_KEY = "AAS_boat_auto_ride";
    private static final String LEGACY_OLDER_ENTITY_IDS_KEY = "head_boat_allow_ride";
    private static final String ADDED_BOAT_IDS_KEY = "AAS_boat";
    private static final String ADDED_SADDLE_IDS_KEY = "AAS_saddle";
    private static final String LEGACY_ADDED_BOAT_IDS_KEY = "added_boat";
    private static final String LEGACY_ADDED_SADDLE_IDS_KEY = "added_saddle";

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(AdvancedAccessorySystemMod.MOD_ID + ".json");
    private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("AdvancedAccessorySystem.json");

    private static BoatAutoPickUpRules autoPickUpRules = BoatAutoPickUpRules.createDefault();
    private static ChargeConfigData chargeConfig = ChargeConfigData.DEFAULT;
    private static BoatAutoPickUpRules addedBoatRules = BoatAutoPickUpRules.createDefaultAddedBoatRules();
    private static BoatAutoPickUpRules addedSaddleRules = BoatAutoPickUpRules.createDefaultAddedSaddleRules();
    private static String autoPickUpJson = BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON;
    private static String chargeJson = BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON;
    private static String addedBoatIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON;
    private static String addedSaddleIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON;
    private static FileTime lastKnownModifiedTime;
    private static boolean initialized;

    private BoatPassengerWhitelistConfig() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        loadOrCreate(false);
    }

    public static void tick() {
        init();
        reloadIfChanged();
    }

    public static String getBoatAutoPickUpJson() {
        init();
        return autoPickUpJson;
    }

    public static String getChargeJson() {
        init();
        return chargeJson;
    }

    public static ChargeConfigData getChargeConfig() {
        init();
        return chargeConfig;
    }

    public static void updateChargeConfigValues(double increaseValue, int chargeTime) {
        init();
        chargeConfig = chargeConfig.withValues(increaseValue, chargeTime);
        chargeJson = chargeConfig.toJsonString();
    }

    public static String getAddedBoatIdsJson() {
        init();
        return addedBoatIdsJson;
    }

    public static BoatAutoPickUpRules getAddedBoatRules() {
        init();
        return addedBoatRules;
    }

    public static String getAddedSaddleIdsJson() {
        init();
        return addedSaddleIdsJson;
    }

    public static BoatAutoPickUpRules getAddedSaddleRules() {
        init();
        return addedSaddleRules;
    }

    public static BoatAutoPickUpRules.ToggleResult toggleEntityTypeId(Identifier entityTypeId) {
        init();
        reloadIfChanged();

        if (entityTypeId == null) {
            return BoatAutoPickUpRules.ToggleResult.NONE;
        }

        BoatAutoPickUpRules.ToggleResult result = autoPickUpRules.toggle(entityTypeId);
        if (result == BoatAutoPickUpRules.ToggleResult.NONE) {
            return result;
        }

        autoPickUpJson = autoPickUpRules.toJsonString();
        saveCurrentState();
        BoatPassengerSettingsSyncClient.onLocalConfigChanged();
        return result;
    }

    public static void writeToConfigRoot(JsonObject rootObject) {
        rootObject.remove(LEGACY_ENTITY_IDS_KEY);
        rootObject.remove(LEGACY_OLDER_ENTITY_IDS_KEY);
        rootObject.remove(LEGACY_ADDED_BOAT_IDS_KEY);
        rootObject.remove(LEGACY_ADDED_SADDLE_IDS_KEY);
        rootObject.add(AUTO_PICK_UP_KEY, autoPickUpRules.toJsonObject());
        rootObject.add(CHARGE_KEY, chargeConfig.toJsonObject());
        rootObject.add(ADDED_BOAT_IDS_KEY, addedBoatRules.toJsonObject());
        rootObject.add(ADDED_SADDLE_IDS_KEY, addedSaddleRules.toJsonObject());
    }

    private static void reloadIfChanged() {
        if (!Files.exists(CONFIG_PATH)) {
            if (lastKnownModifiedTime != null) {
                loadOrCreate(true);
            }
            return;
        }

        try {
            FileTime modifiedTime = Files.getLastModifiedTime(CONFIG_PATH);
            if (!Objects.equals(modifiedTime, lastKnownModifiedTime)) {
                loadOrCreate(true);
            }
        } catch (IOException ignored) {
        }
    }

    private static void loadOrCreate(boolean notifyOnChange) {
        String previousAutoPickUpJson = autoPickUpJson;
        String previousChargeJson = chargeJson;
        String previousAddedBoatIdsJson = addedBoatIdsJson;
        String previousAddedSaddleIdsJson = addedSaddleIdsJson;

        boolean mainConfigExists = Files.exists(CONFIG_PATH);
        JsonObject mainRootObject = readJsonObject(CONFIG_PATH);
        if (mainConfigExists && mainRootObject == null) {
            updateKnownModifiedTime();
            return;
        }

        JsonObject legacyRootObject = null;
        boolean migratedFromLegacy = false;
        if ((mainRootObject == null || !containsAutoPickUpData(mainRootObject)) && Files.exists(LEGACY_CONFIG_PATH)) {
            legacyRootObject = readJsonObject(LEGACY_CONFIG_PATH);
            if (legacyRootObject != null && containsAutoPickUpData(legacyRootObject)) {
                migratedFromLegacy = true;
            }
        }

        JsonElement mainAutoPickUpElement = getAutoPickUpElement(mainRootObject);
        JsonElement legacyAutoPickUpElement = getAutoPickUpElement(legacyRootObject);
        JsonElement mainChargeElement = getChargeElement(mainRootObject);
        JsonElement mainAddedBoatElement = getAddedBoatIdsElement(mainRootObject);
        JsonElement mainAddedSaddleElement = getAddedSaddleIdsElement(mainRootObject);

        String rawAutoPickUpJson = mainAutoPickUpElement != null
                ? mainAutoPickUpElement.toString()
                : legacyAutoPickUpElement != null
                ? legacyAutoPickUpElement.toString()
                : BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON;
        String rawChargeJson = mainChargeElement != null
                ? mainChargeElement.toString()
                : BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON;
        String rawAddedBoatIdsJson = mainAddedBoatElement != null
                ? mainAddedBoatElement.toString()
                : BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON;
        String rawAddedSaddleIdsJson = mainAddedSaddleElement != null
                ? mainAddedSaddleElement.toString()
                : BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON;

        applyAutoPickUpRules(BoatAutoPickUpRules.parse(rawAutoPickUpJson));
        applyChargeConfig(ChargeConfigData.of(rawChargeJson));
        applyAddedBoatPatterns(BoatPassengerConfigHelper.parseAddedBoatPatterns(rawAddedBoatIdsJson));
        applyAddedSaddlePatterns(BoatPassengerConfigHelper.parseAddedSaddlePatterns(rawAddedSaddleIdsJson));

        boolean rewriteSanitizedConfig = !Objects.equals(rawAutoPickUpJson, autoPickUpJson)
                || !Objects.equals(rawChargeJson, chargeJson)
                || !Objects.equals(rawAddedBoatIdsJson, addedBoatIdsJson)
                || !Objects.equals(rawAddedSaddleIdsJson, addedSaddleIdsJson);
        boolean shouldWriteMainConfig = migratedFromLegacy
                || rewriteSanitizedConfig
                || mainRootObject == null
                || !containsPrimaryAutoPickUp(mainRootObject)
                || !containsPrimaryCharge(mainRootObject)
                || !containsPrimaryAddedBoatIds(mainRootObject)
                || !containsPrimaryAddedSaddleIds(mainRootObject)
                || containsLegacyKeys(mainRootObject);

        if (shouldWriteMainConfig) {
            saveCurrentState();
            if (migratedFromLegacy) {
                deleteLegacyConfigIfPresent();
            }
        } else {
            updateKnownModifiedTime();
        }

        notifyIfChanged(
                previousAutoPickUpJson,
                previousChargeJson,
                previousAddedBoatIdsJson,
                previousAddedSaddleIdsJson,
                notifyOnChange
        );
    }

    private static void applyAutoPickUpRules(BoatAutoPickUpRules rules) {
        autoPickUpRules = rules.copy();
        autoPickUpJson = autoPickUpRules.toJsonString();
    }

    private static void applyChargeConfig(ChargeConfigData newChargeConfig) {
        chargeConfig = newChargeConfig;
        chargeJson = chargeConfig.toJsonString();
        AdvancedAccessorySystemConfigs.applyChargeConfigValues(chargeConfig);
    }

    private static void applyAddedBoatPatterns(BoatAutoPickUpRules rules) {
        addedBoatRules = rules.copy();
        addedBoatIdsJson = addedBoatRules.toJsonString();
        AccessoryPatternRuntimeState.setAddedBoatPatterns(addedBoatRules);
    }

    private static void applyAddedSaddlePatterns(BoatAutoPickUpRules rules) {
        addedSaddleRules = rules.copy();
        addedSaddleIdsJson = addedSaddleRules.toJsonString();
        AccessoryPatternRuntimeState.setAddedSaddlePatterns(addedSaddleRules);
    }

    private static void saveCurrentState() {
        JsonObject rootObject = readJsonObject(CONFIG_PATH);
        if (rootObject == null) {
            rootObject = new JsonObject();
        }

        writeToConfigRoot(rootObject);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(rootObject, writer);
            }
            updateKnownModifiedTime();
        } catch (IOException ignored) {
        }
    }

    private static void notifyIfChanged(
            String previousAutoPickUpJson,
            String previousChargeJson,
            String previousAddedBoatIdsJson,
            String previousAddedSaddleIdsJson,
            boolean notifyOnChange
    ) {
        if (notifyOnChange
                && (!Objects.equals(previousAutoPickUpJson, autoPickUpJson)
                || !Objects.equals(previousChargeJson, chargeJson)
                || !Objects.equals(previousAddedBoatIdsJson, addedBoatIdsJson)
                || !Objects.equals(previousAddedSaddleIdsJson, addedSaddleIdsJson))) {
            BoatPassengerSettingsSyncClient.onLocalConfigChanged();
        }
    }



    private static JsonObject readJsonObject(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        try {
            String rawText = Files.readString(path, StandardCharsets.UTF_8);
            JsonElement rootElement = JsonParser.parseString(stripJsonComments(rawText));
            return rootElement instanceof JsonObject rootObject ? rootObject : null;
        } catch (IOException | IllegalStateException | JsonParseException ignored) {
            return null;
        }
    }

    private static String stripJsonComments(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(rawText.length());
        boolean inString = false;
        boolean escaping = false;
        boolean lineComment = false;
        boolean blockComment = false;

        for (int i = 0; i < rawText.length(); i++) {
            char current = rawText.charAt(i);
            char next = i + 1 < rawText.length() ? rawText.charAt(i + 1) : '\0';

            if (lineComment) {
                if (current == '\n' || current == '\r') {
                    lineComment = false;
                    builder.append(current);
                }
                continue;
            }

            if (blockComment) {
                if (current == '*' && next == '/') {
                    blockComment = false;
                    i++;
                }
                continue;
            }

            if (!inString && current == '/' && next == '/') {
                lineComment = true;
                i++;
                continue;
            }

            if (!inString && current == '/' && next == '*') {
                blockComment = true;
                i++;
                continue;
            }

            builder.append(current);

            if (escaping) {
                escaping = false;
                continue;
            }

            if (current == '\\') {
                escaping = true;
                continue;
            }

            if (current == '"') {
                inString = !inString;
            }
        }

        return builder.toString().replace("\uFEFF", "");
    }

    private static boolean containsAutoPickUpData(JsonObject rootObject) {
        return getAutoPickUpElement(rootObject) != null;
    }

    private static boolean containsPrimaryAutoPickUp(JsonObject rootObject) {
        return rootObject != null && rootObject.get(AUTO_PICK_UP_KEY) instanceof JsonObject;
    }

    private static boolean containsPrimaryCharge(JsonObject rootObject) {
        return rootObject != null && rootObject.get(CHARGE_KEY) instanceof JsonObject;
    }

    private static boolean containsPrimaryAddedBoatIds(JsonObject rootObject) {
        return rootObject != null && (rootObject.get(ADDED_BOAT_IDS_KEY) instanceof JsonObject
                || rootObject.get(ADDED_BOAT_IDS_KEY) instanceof JsonArray);
    }

    private static boolean containsPrimaryAddedSaddleIds(JsonObject rootObject) {
        return rootObject != null && (rootObject.get(ADDED_SADDLE_IDS_KEY) instanceof JsonObject
                || rootObject.get(ADDED_SADDLE_IDS_KEY) instanceof JsonArray);
    }

    private static boolean containsLegacyKeys(JsonObject rootObject) {
        return rootObject != null
                && (rootObject.get(LEGACY_ENTITY_IDS_KEY) instanceof JsonArray
                || rootObject.get(LEGACY_OLDER_ENTITY_IDS_KEY) instanceof JsonArray
                || rootObject.get(LEGACY_ADDED_BOAT_IDS_KEY) instanceof JsonArray
                || rootObject.get(LEGACY_ADDED_SADDLE_IDS_KEY) instanceof JsonArray);
    }

    private static JsonElement getAutoPickUpElement(JsonObject rootObject) {
        if (rootObject == null) {
            return null;
        }

        if (rootObject.get(AUTO_PICK_UP_KEY) instanceof JsonObject jsonObject) {
            return jsonObject;
        }

        if (rootObject.get(LEGACY_ENTITY_IDS_KEY) instanceof JsonArray jsonArray) {
            return jsonArray;
        }

        return rootObject.get(LEGACY_OLDER_ENTITY_IDS_KEY) instanceof JsonArray jsonArray ? jsonArray : null;
    }

    private static JsonElement getChargeElement(JsonObject rootObject) {
        return rootObject != null && rootObject.get(CHARGE_KEY) instanceof JsonObject jsonObject ? jsonObject : null;
    }

    private static JsonElement getAddedBoatIdsElement(JsonObject rootObject) {
        if (rootObject == null) {
            return null;
        }

        if (rootObject.get(ADDED_BOAT_IDS_KEY) instanceof JsonElement element) {
            return element;
        }

        return rootObject.get(LEGACY_ADDED_BOAT_IDS_KEY) instanceof JsonArray jsonArray ? jsonArray : null;
    }

    private static JsonElement getAddedSaddleIdsElement(JsonObject rootObject) {
        if (rootObject == null) {
            return null;
        }

        if (rootObject.get(ADDED_SADDLE_IDS_KEY) instanceof JsonElement element) {
            return element;
        }

        return rootObject.get(LEGACY_ADDED_SADDLE_IDS_KEY) instanceof JsonArray jsonArray ? jsonArray : null;
    }

    private static void updateKnownModifiedTime() {
        try {
            lastKnownModifiedTime = Files.exists(CONFIG_PATH) ? Files.getLastModifiedTime(CONFIG_PATH) : null;
        } catch (IOException ignored) {
        }
    }

    private static void deleteLegacyConfigIfPresent() {
        try {
            Files.deleteIfExists(LEGACY_CONFIG_PATH);
        } catch (IOException ignored) {
        }
    }
}
