package com.alonie.advancedaccessorysystem.feature.boatpassenger.sync;

import com.alonie.advancedaccessorysystem.collective.CollectiveNetworkingBridge;
import com.alonie.advancedaccessorysystem.feature.accessory.state.AccessoryPatternRuntimeState;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.c2s.request.BoatPassengerSettingsRequestPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.s2c.sync.BoatPassengerSettingsSyncPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerServerState;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerSettingsState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class BoatPassengerSettingsSyncManager {
    private BoatPassengerSettingsSyncManager() {
    }

    public static void register() {
    }

    public static BoatPassengerSettingsState getGlobalSettings() {
        return BoatPassengerServerState.settings();
    }

    /**
     * @deprecated Boat passenger settings are global runtime state for the current server session.
     */
    @Deprecated(forRemoval = false)
    public static BoatPassengerSettingsState getSettings(PlayerEntity player) {
        return getGlobalSettings();
    }

    public static void handleRequest(ServerPlayerEntity player, BoatPassengerSettingsRequestPayload payload) {
        if (payload == null) {
            return;
        }
        updateSettings(
                player == null ? null : player.getEntityWorld().getServer(),
                payload.radius(),
                payload.boatAutoPickUpJson(),
                payload.addedBoatIdsJson(),
                payload.addedSaddleIdsJson(),
                payload.dismountLaunchSpeed(),
                payload.chargeJson()
        );
    }

    public static void onPlayerLoggedIn(ServerPlayerEntity player) {
        syncTo(player);
    }

    public static void resetRuntimeState() {
        BoatPassengerServerState.reset();
        applyAccessoryPatterns(
                BoatPassengerServerState.addedBoatIdsJson(),
                BoatPassengerServerState.addedSaddleIdsJson()
        );
    }

    private static void updateSettings(
            MinecraftServer server,
            double radius,
            String boatAutoPickUpJson,
            String addedBoatIdsJson,
            String addedSaddleIdsJson,
            double dismountLaunchSpeed,
            String chargeJson
    ) {
        BoatPassengerServerState.setBoatAutoPickUpJson(boatAutoPickUpJson);
        BoatPassengerServerState.setAddedBoatIdsJson(addedBoatIdsJson);
        BoatPassengerServerState.setAddedSaddleIdsJson(addedSaddleIdsJson);
        BoatPassengerServerState.setChargeJson(chargeJson);
        BoatPassengerServerState.setSettings(BoatPassengerSettingsState.of(
                radius,
                BoatPassengerServerState.boatAutoPickUpJson(),
                dismountLaunchSpeed,
                BoatPassengerServerState.chargeJson()
        ));
        applyAccessoryPatterns(
                BoatPassengerServerState.addedBoatIdsJson(),
                BoatPassengerServerState.addedSaddleIdsJson()
        );

        if (server != null) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                syncTo(player);
            }
        }
    }

    private static void syncTo(ServerPlayerEntity player) {
        if (player == null) {
            return;
        }

        CollectiveNetworkingBridge.sendToClient(new BoatPassengerSettingsSyncPayload(
                BoatPassengerServerState.settings().radius(),
                BoatPassengerServerState.boatAutoPickUpJson(),
                BoatPassengerServerState.addedBoatIdsJson(),
                BoatPassengerServerState.addedSaddleIdsJson(),
                BoatPassengerServerState.settings().dismountLaunchSpeed(),
                BoatPassengerServerState.chargeJson()
        ), player);
    }

    private static void applyAccessoryPatterns(String addedBoatIdsJson, String addedSaddleIdsJson) {
        AccessoryPatternRuntimeState.setAddedBoatPatterns(
                BoatPassengerConfigHelper.parseAddedBoatPatterns(addedBoatIdsJson)
        );
        AccessoryPatternRuntimeState.setAddedSaddlePatterns(
                BoatPassengerConfigHelper.parseAddedSaddlePatterns(addedSaddleIdsJson)
        );
    }
}
