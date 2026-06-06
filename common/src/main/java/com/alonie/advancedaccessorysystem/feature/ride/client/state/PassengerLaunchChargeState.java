package com.alonie.advancedaccessorysystem.feature.ride.client.state;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.ChargeConfigData;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerSettingsState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * Client-only runtime state for charged passenger launch input.
 */
public final class PassengerLaunchChargeState {
    private static double chargedLaunchSpeed;
    private static int chargedTicks;
    private static boolean chargeActive;

    private PassengerLaunchChargeState() {
    }

    public static boolean hasChargedLaunchOverride() {
        return chargedLaunchSpeed > 0.0D;
    }

    public static double getChargedLaunchSpeed() {
        return chargedLaunchSpeed;
    }

    public static void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            resetCharge();
            return;
        }

        Entity chargeTarget = getChargeTarget(client.player);
        if (chargeTarget == null || !AdvancedAccessorySystemConfigs.chargePassengerLaunchHotkey.isDown()) {
            resetCharge();
            return;
        }

        ChargeConfigData chargeConfig = BoatPassengerSettingsSyncClient.getCurrentSettings().chargeConfig();
        int chargeTime = chargeConfig.chargeTime();
        if (chargedTicks < chargeTime) {
            chargedLaunchSpeed = BoatPassengerConfigHelper.sanitizeDismountLaunchSpeed(
                    chargedLaunchSpeed + chargeConfig.increaseValue()
            );
            chargedTicks++;
        }
        chargeActive = true;
        client.player.displayClientMessage(Component.literal(String.format(Locale.ROOT, "%.2f", chargedLaunchSpeed)), true);
    }

    private static Entity getChargeTarget(Player player) {
        BoatPassengerSettingsState settings = BoatPassengerSettingsSyncClient.getCurrentSettings();
        if (settings == null) return null;
        List<Entity> passengers = player.getPassengers();
        for (Entity passenger : passengers) {
            if (settings.allowsChargedLaunch(passenger)) {
                return passenger;
            }
        }

        return null;
    }

    private static void resetCharge() {
        Minecraft client = Minecraft.getInstance();
        if (chargeActive && client != null && client.player != null) {
            client.player.displayClientMessage(Component.empty(), true);
        }
        chargedLaunchSpeed = 0.0D;
        chargedTicks = 0;
        chargeActive = false;
    }
}
