package com.alonie.advancedaccessorysystem.collective;

import com.natamus.collective_common_fabric.functions.TaskFunctions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CollectiveTaskBridge {
    private CollectiveTaskBridge() {
    }

    public static void runServer(MinecraftServer server, Runnable runnable) {
        if (server == null || runnable == null) {
            return;
        }
        TaskFunctions.enqueueCollectiveServerTask(server, runnable, 0);
    }

    public static void runServer(ServerPlayerEntity player, Runnable runnable) {
        if (player == null) {
            return;
        }
        runServer(player.getEntityWorld().getServer(), runnable);
    }

    public static void runClient(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        TaskFunctions.enqueueCollectiveClientTask(runnable, 0);
    }
}
