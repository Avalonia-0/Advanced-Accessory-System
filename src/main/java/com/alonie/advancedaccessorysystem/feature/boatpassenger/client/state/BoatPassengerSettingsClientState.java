package com.alonie.advancedaccessorysystem.feature.boatpassenger.client.state;

import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerSettingsState;

import java.util.Objects;

/**
 * Client-side cache for boat passenger settings and local resend tracking.
 */
public final class BoatPassengerSettingsClientState {
    private static double lastSentRadius = Double.NaN;
    private static String lastSentBoatAutoPickUpJson;
    private static String lastSentAddedBoatIdsJson;
    private static String lastSentAddedSaddleIdsJson;
    private static double lastSentDismountLaunchSpeed = Double.NaN;
    private static String lastSentChargeJson;
    private static boolean localStateDirty = true;

    private static BoatPassengerSettingsState currentSettings = BoatPassengerSettingsState.DEFAULT;
    private static String currentAddedBoatIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON;
    private static String currentAddedSaddleIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON;

    private BoatPassengerSettingsClientState() {
    }

    public static BoatPassengerSettingsState currentSettings() {
        return currentSettings;
    }

    public static void setCurrentSettings(BoatPassengerSettingsState settings) {
        currentSettings = settings == null ? BoatPassengerSettingsState.DEFAULT : settings;
    }

    public static String currentAddedBoatIdsJson() {
        return currentAddedBoatIdsJson;
    }

    public static void setCurrentAddedBoatIdsJson(String json) {
        currentAddedBoatIdsJson = json == null
                ? BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON
                : json;
    }

    public static String currentAddedSaddleIdsJson() {
        return currentAddedSaddleIdsJson;
    }

    public static void setCurrentAddedSaddleIdsJson(String json) {
        currentAddedSaddleIdsJson = json == null
                ? BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON
                : json;
    }

    public static void markLocalStateDirty() {
        localStateDirty = true;
    }

    public static boolean shouldSend(
            double radius,
            String boatAutoPickUpJson,
            String addedBoatIdsJson,
            String addedSaddleIdsJson,
            double dismountLaunchSpeed,
            String chargeJson
    ) {
        return localStateDirty
                || Double.compare(radius, lastSentRadius) != 0
                || !Objects.equals(boatAutoPickUpJson, lastSentBoatAutoPickUpJson)
                || !Objects.equals(addedBoatIdsJson, lastSentAddedBoatIdsJson)
                || !Objects.equals(addedSaddleIdsJson, lastSentAddedSaddleIdsJson)
                || Double.compare(dismountLaunchSpeed, lastSentDismountLaunchSpeed) != 0
                || !Objects.equals(chargeJson, lastSentChargeJson);
    }

    public static void recordSentState(
            double radius,
            String boatAutoPickUpJson,
            String addedBoatIdsJson,
            String addedSaddleIdsJson,
            double dismountLaunchSpeed,
            String chargeJson
    ) {
        lastSentRadius = radius;
        lastSentBoatAutoPickUpJson = boatAutoPickUpJson;
        lastSentAddedBoatIdsJson = addedBoatIdsJson;
        lastSentAddedSaddleIdsJson = addedSaddleIdsJson;
        lastSentDismountLaunchSpeed = dismountLaunchSpeed;
        lastSentChargeJson = chargeJson;
        localStateDirty = false;
    }

    public static void resetConnectionState() {
        lastSentRadius = Double.NaN;
        lastSentBoatAutoPickUpJson = null;
        lastSentAddedBoatIdsJson = null;
        lastSentAddedSaddleIdsJson = null;
        lastSentDismountLaunchSpeed = Double.NaN;
        lastSentChargeJson = null;
        localStateDirty = true;
        currentSettings = BoatPassengerSettingsState.DEFAULT;
        currentAddedBoatIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_BOAT_IDS_JSON;
        currentAddedSaddleIdsJson = BoatPassengerConfigHelper.DEFAULT_ADDED_SADDLE_IDS_JSON;
    }
}
