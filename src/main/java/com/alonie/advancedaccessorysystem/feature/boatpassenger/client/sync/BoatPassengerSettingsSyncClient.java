package com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.collective.CollectiveNetworkingBridge;
import com.alonie.advancedaccessorysystem.feature.accessory.state.AccessoryPatternRuntimeState;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.config.BoatPassengerWhitelistConfig;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.state.BoatPassengerSettingsClientState;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.c2s.request.BoatPassengerSettingsRequestPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.s2c.sync.BoatPassengerSettingsSyncPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerSettingsState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

public final class BoatPassengerSettingsSyncClient {
    private BoatPassengerSettingsSyncClient() {
    }

    public static void register() {
        BoatPassengerWhitelistConfig.init();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetAllState());
        ClientTickEvents.END_CLIENT_TICK.register(BoatPassengerSettingsSyncClient::onEndClientTick);
    }

    public static void onClientWorldLoad() {
        BoatPassengerSettingsClientState.resetConnectionState();
    }

    public static BoatPassengerSettingsState getCurrentSettings() {
        return BoatPassengerSettingsClientState.currentSettings();
    }

    public static void onLocalConfigChanged() {
        BoatPassengerSettingsClientState.markLocalStateDirty();
    }

    private static void onEndClientTick(MinecraftClient client) {
        BoatPassengerWhitelistConfig.tick();

        if (client.world == null || client.player == null) {
            resetAllState();
            return;
        }

        if (AdvancedAccessorySystemConfigs.isLocalServerControlAvailable()) {
            BoatPassengerSettingsClientState.setCurrentSettings(createLocalSettings());
            BoatPassengerSettingsClientState.setCurrentAddedBoatIdsJson(BoatPassengerWhitelistConfig.getAddedBoatIdsJson());
            BoatPassengerSettingsClientState.setCurrentAddedSaddleIdsJson(BoatPassengerWhitelistConfig.getAddedSaddleIdsJson());
            applyLocalAccessoryPatterns();
            syncLocalSettings();
            return;
        }

        applySyncedAccessoryPatterns();
    }

    private static void syncLocalSettings() {
        double radius = AdvancedAccessorySystemConfigs.getBoatPassengerAutoRideRadius();
        String boatAutoPickUpJson = BoatPassengerWhitelistConfig.getBoatAutoPickUpJson();
        String addedBoatIdsJson = BoatPassengerWhitelistConfig.getAddedBoatIdsJson();
        String addedSaddleIdsJson = BoatPassengerWhitelistConfig.getAddedSaddleIdsJson();
        double dismountLaunchSpeed = AdvancedAccessorySystemConfigs.getDismountPassengerLaunchSpeed();
        String chargeJson = BoatPassengerWhitelistConfig.getChargeJson();
        if (!BoatPassengerSettingsClientState.shouldSend(
                radius,
                boatAutoPickUpJson,
                addedBoatIdsJson,
                addedSaddleIdsJson,
                dismountLaunchSpeed,
                chargeJson
        )) {
            return;
        }

        CollectiveNetworkingBridge.sendToServer(new BoatPassengerSettingsRequestPayload(
                radius,
                boatAutoPickUpJson,
                addedBoatIdsJson,
                addedSaddleIdsJson,
                dismountLaunchSpeed,
                chargeJson
        ));
        BoatPassengerSettingsClientState.recordSentState(
                radius,
                boatAutoPickUpJson,
                addedBoatIdsJson,
                addedSaddleIdsJson,
                dismountLaunchSpeed,
                chargeJson
        );
    }

    public static void handleSync(BoatPassengerSettingsSyncPayload payload) {
        BoatPassengerSettingsClientState.setCurrentSettings(BoatPassengerSettingsState.of(
                payload.radius(),
                payload.boatAutoPickUpJson(),
                payload.dismountLaunchSpeed(),
                payload.chargeJson()
        ));
        BoatPassengerSettingsClientState.setCurrentAddedBoatIdsJson(payload.addedBoatIdsJson());
        BoatPassengerSettingsClientState.setCurrentAddedSaddleIdsJson(payload.addedSaddleIdsJson());
        applySyncedAccessoryPatterns();
    }

    private static BoatPassengerSettingsState createLocalSettings() {
        return BoatPassengerSettingsState.of(
                AdvancedAccessorySystemConfigs.getBoatPassengerAutoRideRadius(),
                BoatPassengerWhitelistConfig.getBoatAutoPickUpJson(),
                AdvancedAccessorySystemConfigs.getDismountPassengerLaunchSpeed(),
                BoatPassengerWhitelistConfig.getChargeJson()
        );
    }

    private static void applyLocalAccessoryPatterns() {
        AccessoryPatternRuntimeState.setAddedBoatPatterns(BoatPassengerWhitelistConfig.getAddedBoatPatterns());
        AccessoryPatternRuntimeState.setAddedSaddlePatterns(BoatPassengerWhitelistConfig.getAddedSaddlePatterns());
    }

    private static void applySyncedAccessoryPatterns() {
        AccessoryPatternRuntimeState.setAddedBoatPatterns(
                BoatPassengerConfigHelper.parseAddedBoatPatterns(BoatPassengerSettingsClientState.currentAddedBoatIdsJson())
        );
        AccessoryPatternRuntimeState.setAddedSaddlePatterns(
                BoatPassengerConfigHelper.parseAddedSaddlePatterns(BoatPassengerSettingsClientState.currentAddedSaddleIdsJson())
        );
    }

    private static void resetAllState() {
        BoatPassengerSettingsClientState.resetConnectionState();
        applyLocalAccessoryPatterns();
    }
}
