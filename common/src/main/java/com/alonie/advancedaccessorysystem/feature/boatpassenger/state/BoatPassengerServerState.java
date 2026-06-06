package com.alonie.advancedaccessorysystem.feature.boatpassenger.state;

import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;

/**
 * Server-session runtime state for boat passenger settings sync.
 */
public final class BoatPassengerServerState {
    private static BoatPassengerSettingsState globalSettings = BoatPassengerSettingsState.DEFAULT;
    private static String globalBoatAutoPickUpJson = BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON;
    private static String globalAddedBoatIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON;
    private static String globalAddedSaddleIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON;
    private static String globalChargeJson = BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON;

    private BoatPassengerServerState() {
    }

    public static BoatPassengerSettingsState settings() {
        return globalSettings;
    }

    public static void setSettings(BoatPassengerSettingsState settings) {
        globalSettings = settings == null ? BoatPassengerSettingsState.DEFAULT : settings;
    }

    public static String boatAutoPickUpJson() {
        return globalBoatAutoPickUpJson;
    }

    public static void setBoatAutoPickUpJson(String json) {
        globalBoatAutoPickUpJson = json == null
                ? BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON
                : json;
    }

    public static String addedBoatIdsJson() {
        return globalAddedBoatIdsJson;
    }

    public static void setAddedBoatIdsJson(String json) {
        globalAddedBoatIdsJson = json == null
                ? BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON
                : json;
    }

    public static String addedSaddleIdsJson() {
        return globalAddedSaddleIdsJson;
    }

    public static void setAddedSaddleIdsJson(String json) {
        globalAddedSaddleIdsJson = json == null
                ? BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON
                : json;
    }

    public static String chargeJson() {
        return globalChargeJson;
    }

    public static void setChargeJson(String json) {
        globalChargeJson = json == null
                ? BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON
                : json;
    }

    public static void reset() {
        globalSettings = BoatPassengerSettingsState.DEFAULT;
        globalBoatAutoPickUpJson = BoatPassengerConfigHelper.DEFAULT_BOAT_AUTO_PICK_UP_JSON;
        globalAddedBoatIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON;
        globalAddedSaddleIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON;
        globalChargeJson = BoatPassengerConfigHelper.DEFAULT_CHARGE_JSON;
    }
}
