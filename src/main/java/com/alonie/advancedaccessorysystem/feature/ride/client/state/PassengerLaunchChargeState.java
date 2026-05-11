package com.alonie.advancedaccessorysystem.feature.ride.client.state;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.ChargeConfigData;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerSettingsState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

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

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(PassengerLaunchChargeState::onEndClientTick);
    }

    public static boolean hasChargedLaunchOverride() {
        return chargedLaunchSpeed > 0.0D;
    }

    public static double getChargedLaunchSpeed() {
        return chargedLaunchSpeed;
    }

    private static void onEndClientTick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            resetCharge();
            return;
        }

        Entity chargeTarget = getChargeTarget(client.player);
        if (chargeTarget == null || !AdvancedAccessorySystemConfigs.CHARGE_PASSENGER_LAUNCH_HOTKEY.getKeybind().isKeybindHeld()) {
            resetCharge();
            return;
        }

        ChargeConfigData chargeConfig = BoatPassengerSettingsSyncClient.getCurrentSettings().chargeConfig();
        int chargeTime = chargeConfig.chargeTime();
        if (chargedTicks < chargeTime) {
            chargedLaunchSpeed = BoatPassengerConfigHelper.sanitizeDismountLaunchSpeed(
                    chargedLaunchSpeed + chargeConfig.increaseValue()
            );
        }

        chargedTicks++;
        chargeActive = true;
        client.player.sendMessage(Text.literal(String.format(Locale.ROOT, "%.2f", chargedLaunchSpeed)), true);
    }

    private static Entity getChargeTarget(PlayerEntity player) {
        BoatPassengerSettingsState settings = BoatPassengerSettingsSyncClient.getCurrentSettings();
        List<Entity> passengers = player.getPassengerList();
        for (Entity passenger : passengers) {
            if (settings.allowsChargedLaunch(passenger)) {
                return passenger;
            }
        }

        return null;
    }

    private static void resetCharge() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (chargeActive && client != null && client.player != null) {
            client.player.sendMessage(Text.empty(), true);
        }
        chargedLaunchSpeed = 0.0D;
        chargedTicks = 0;
        chargeActive = false;
    }
}
