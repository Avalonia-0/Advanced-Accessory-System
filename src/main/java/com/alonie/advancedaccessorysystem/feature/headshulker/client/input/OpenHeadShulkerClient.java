package com.alonie.advancedaccessorysystem.feature.headshulker.client.input;

import com.alonie.advancedaccessorysystem.collective.CollectiveNetworkingBridge;
import com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request.OpenHeadShulkerPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ShulkerBoxScreenHandler;

public final class OpenHeadShulkerClient {
    private OpenHeadShulkerClient() {
    }

    public static boolean trigger() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return false;
        }

        if (client.currentScreen instanceof HandledScreen<?> && client.player.currentScreenHandler instanceof ShulkerBoxScreenHandler) {
            client.player.closeHandledScreen();
            client.setScreen(null);
            return true;
        }

        if (client.currentScreen != null) {
            return false;
        }

        CollectiveNetworkingBridge.sendToServer(new OpenHeadShulkerPayload());
        return true;
    }
}
