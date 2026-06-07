package com.alonie.advancedaccessorysystem.feature.headshulker.client.input;

import com.alonie.advancedaccessorysystem.bridge.PlatformNetworking;
import com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request.OpenHeadShulkerPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ShulkerBoxMenu;

public final class OpenHeadShulkerClient {
    private OpenHeadShulkerClient() {
    }

    public static boolean trigger() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.level == null) {
            return false;
        }

        if (client.screen instanceof AbstractContainerScreen<?> && client.player.containerMenu instanceof ShulkerBoxMenu) {
            client.player.closeContainer();
            client.setScreen(null);
            return true;
        }

        if (client.screen != null) {
            return false;
        }

        var buf = PlatformNetworking.createBuffer(client.level.registryAccess());
        new OpenHeadShulkerPayload().write(buf);
        PlatformNetworking.sendToServer(OpenHeadShulkerPayload.ID, buf);
        return true;
    }
}
