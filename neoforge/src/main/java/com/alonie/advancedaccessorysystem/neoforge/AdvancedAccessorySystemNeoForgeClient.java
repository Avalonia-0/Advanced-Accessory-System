package com.alonie.advancedaccessorysystem.neoforge;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.bootstrap.ClientBootstrap;
import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = AdvancedAccessorySystemMod.MOD_ID, value = Dist.CLIENT)
public final class AdvancedAccessorySystemNeoForgeClient {
    private AdvancedAccessorySystemNeoForgeClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientBootstrap::register);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(AdvancedAccessorySystemConfigs.openConfigHotkey);
        event.register(AdvancedAccessorySystemConfigs.openHeadShulkerHotkey);
        event.register(AdvancedAccessorySystemConfigs.dismountPassengersHotkey);
        event.register(AdvancedAccessorySystemConfigs.chargePassengerLaunchHotkey);
    }
}
