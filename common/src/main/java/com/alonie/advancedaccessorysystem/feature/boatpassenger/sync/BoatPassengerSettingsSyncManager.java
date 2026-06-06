package com.alonie.advancedaccessorysystem.feature.boatpassenger.sync;

import com.alonie.advancedaccessorysystem.bridge.PlatformNetworking;
import com.alonie.advancedaccessorysystem.feature.accessory.state.AccessoryPatternRuntimeState;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.c2s.request.BoatPassengerSettingsRequestPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.s2c.sync.BoatPassengerSettingsSyncPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerServerState;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerSettingsState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class BoatPassengerSettingsSyncManager {
    private BoatPassengerSettingsSyncManager() {
    }

    public static BoatPassengerSettingsState getGlobalSettings() {
        return BoatPassengerServerState.settings();
    }

    @Deprecated(forRemoval = false)
    public static BoatPassengerSettingsState getSettings(Player player) {
        return getGlobalSettings();
    }

    public static void handleRequest(ServerPlayer player, BoatPassengerSettingsRequestPayload payload) {
        if (payload == null) return;
        updateSettings(
                player == null ? null : player.level().getServer(),
                payload.radius(), payload.boatAutoPickUpJson(),
                payload.addedBoatIdsJson(), payload.addedSaddleIdsJson(),
                payload.dismountLaunchSpeed(), payload.chargeJson()
        );
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        syncTo(player);
    }

    public static void resetRuntimeState() {
        BoatPassengerServerState.reset();
        applyAccessoryPatterns(
                BoatPassengerServerState.addedBoatIdsJson(),
                BoatPassengerServerState.addedSaddleIdsJson()
        );
    }

    private static void updateSettings(MinecraftServer server, double radius, String boatAutoPickUpJson,
                                        String addedBoatIdsJson, String addedSaddleIdsJson,
                                        double dismountLaunchSpeed, String chargeJson) {
        BoatPassengerServerState.setBoatAutoPickUpJson(boatAutoPickUpJson);
        BoatPassengerServerState.setAddedBoatIdsJson(addedBoatIdsJson);
        BoatPassengerServerState.setAddedSaddleIdsJson(addedSaddleIdsJson);
        BoatPassengerServerState.setChargeJson(chargeJson);
        BoatPassengerServerState.setSettings(BoatPassengerSettingsState.of(radius, BoatPassengerServerState.boatAutoPickUpJson(), dismountLaunchSpeed, BoatPassengerServerState.chargeJson()));
        applyAccessoryPatterns(BoatPassengerServerState.addedBoatIdsJson(), BoatPassengerServerState.addedSaddleIdsJson());

        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                syncTo(player);
            }
        }
    }

    private static void syncTo(ServerPlayer player) {
        if (player == null) return;

        var buf = PlatformNetworking.createBuffer(player.registryAccess());
        new BoatPassengerSettingsSyncPayload(
                BoatPassengerServerState.settings().radius(),
                BoatPassengerServerState.boatAutoPickUpJson(),
                BoatPassengerServerState.addedBoatIdsJson(),
                BoatPassengerServerState.addedSaddleIdsJson(),
                BoatPassengerServerState.settings().dismountLaunchSpeed(),
                BoatPassengerServerState.chargeJson()
        ).write(buf);
        PlatformNetworking.sendToClient(BoatPassengerSettingsSyncPayload.ID, buf, player);
    }

    private static void applyAccessoryPatterns(String addedBoatIdsJson, String addedSaddleIdsJson) {
        AccessoryPatternRuntimeState.setAddedBoatPatterns(BoatPassengerConfigHelper.parseAddedBoatPatterns(addedBoatIdsJson));
        AccessoryPatternRuntimeState.setAddedSaddlePatterns(BoatPassengerConfigHelper.parseAddedSaddlePatterns(addedSaddleIdsJson));
    }
}
