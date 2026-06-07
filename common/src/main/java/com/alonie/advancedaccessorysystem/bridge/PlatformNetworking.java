package com.alonie.advancedaccessorysystem.bridge;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request.ArmorVisibilityUpdatePayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.sync.ArmorVisibilitySyncManager;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.c2s.request.BoatPassengerSettingsRequestPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;
import com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request.OpenHeadShulkerPayload;
import com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request.OpenHeadShulkerRequestHandler;
import com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request.DismountPassengersPayload;
import com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request.DismountPassengersRequestHandler;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Networking bridge backed by Architectury raw API (NeoForge compatible).
 * C2S (client→server) packet registration only.
 * S2C (server→client) registrations are in {@code PlatformNetworkingClient}.
 */
public final class PlatformNetworking {
    private PlatformNetworking() {
    }

    /** Register C2S packet handlers (runs on both sides — server handles them). */
    public static void registerC2S() {
        // -- C2S: Armor Visibility Update --
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ArmorVisibilityUpdatePayload.ID,
                (buf, ctx) -> {
                    ArmorVisibilityUpdatePayload p = ArmorVisibilityUpdatePayload.read(buf);
                    ctx.queue(() -> ArmorVisibilitySyncManager.handleUpdate((ServerPlayer) ctx.getPlayer(), p));
                });

        // -- C2S: Boat Passenger Settings Request --
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BoatPassengerSettingsRequestPayload.ID,
                (buf, ctx) -> {
                    BoatPassengerSettingsRequestPayload p = BoatPassengerSettingsRequestPayload.read(buf);
                    ctx.queue(() -> BoatPassengerSettingsSyncManager.handleRequest((ServerPlayer) ctx.getPlayer(), p));
                });

        // -- C2S: Open Head Shulker --
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, OpenHeadShulkerPayload.ID,
                (buf, ctx) -> {
                    OpenHeadShulkerPayload p = OpenHeadShulkerPayload.read(buf);
                    ctx.queue(() -> OpenHeadShulkerRequestHandler.handleRequest((ServerPlayer) ctx.getPlayer(), p));
                });

        // -- C2S: Dismount Passengers --
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, DismountPassengersPayload.ID,
                (buf, ctx) -> {
                    DismountPassengersPayload p = DismountPassengersPayload.read(buf);
                    ctx.queue(() -> DismountPassengersRequestHandler.handleRequest((ServerPlayer) ctx.getPlayer(), p));
                });
    }

    /** Shared utility — create a buffer with registry access. */
    public static RegistryFriendlyByteBuf createBuffer(RegistryAccess registryAccess) {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
    }

    /** Send a packet from client to server. */
    public static void sendToServer(Identifier id, RegistryFriendlyByteBuf buf) {
        NetworkManager.sendToServer(id, buf);
    }

    /** Send a packet from server to a specific client. */
    public static void sendToClient(Identifier id, RegistryFriendlyByteBuf buf, ServerPlayer player) {
        if (player == null) return;
        NetworkManager.sendToPlayer(player, id, buf);
    }

    /** Broadcast a packet from server to all connected clients. */
    public static void sendToAllClients(Identifier id, RegistryFriendlyByteBuf buf, MinecraftServer server) {
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            RegistryFriendlyByteBuf copy = new RegistryFriendlyByteBuf(
                    Unpooled.copiedBuffer(buf), server.registryAccess());
            NetworkManager.sendToPlayer(p, id, copy);
        }
    }
}
