package com.alonie.advancedaccessorysystem.bridge;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request.ArmorVisibilityUpdatePayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.sync.ArmorVisibilitySyncManager;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.c2s.request.BoatPassengerSettingsRequestPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;
import com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request.OpenHeadShulkerPayload;
import com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request.OpenHeadShulkerRequestHandler;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PlatformNetworking {
    private PlatformNetworking() {
    }

    public static void registerC2S() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ArmorVisibilityUpdatePayload.ID,
                (buf, ctx) -> {
                    ArmorVisibilityUpdatePayload p = ArmorVisibilityUpdatePayload.read(buf);
                    ctx.queue(() -> ArmorVisibilitySyncManager.handleUpdate((ServerPlayer) ctx.getPlayer(), p));
                });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BoatPassengerSettingsRequestPayload.ID,
                (buf, ctx) -> {
                    BoatPassengerSettingsRequestPayload p = BoatPassengerSettingsRequestPayload.read(buf);
                    ctx.queue(() -> BoatPassengerSettingsSyncManager.handleRequest((ServerPlayer) ctx.getPlayer(), p));
                });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, OpenHeadShulkerPayload.ID,
                (buf, ctx) -> {
                    OpenHeadShulkerPayload p = OpenHeadShulkerPayload.read(buf);
                    ctx.queue(() -> OpenHeadShulkerRequestHandler.handleRequest((ServerPlayer) ctx.getPlayer(), p));
                });
    }

    public static RegistryFriendlyByteBuf createBuffer(RegistryAccess registryAccess) {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
    }

    public static void sendToServer(Identifier id, RegistryFriendlyByteBuf buf) {
        NetworkManager.sendToServer(id, buf);
    }

    public static void sendToClient(Identifier id, RegistryFriendlyByteBuf buf, ServerPlayer player) {
        if (player == null) return;
        NetworkManager.sendToPlayer(player, id, buf);
    }

    public static void sendToAllClients(Identifier id, RegistryFriendlyByteBuf buf, MinecraftServer server) {
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            RegistryFriendlyByteBuf copy = new RegistryFriendlyByteBuf(
                    Unpooled.copiedBuffer(buf), server.registryAccess());
            NetworkManager.sendToPlayer(p, id, copy);
        }
    }
}
