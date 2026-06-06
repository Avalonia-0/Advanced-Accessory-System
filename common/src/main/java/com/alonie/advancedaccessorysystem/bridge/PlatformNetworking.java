package com.alonie.advancedaccessorysystem.bridge;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.c2s.request.ArmorVisibilityUpdatePayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.network.s2c.sync.ArmorVisibilitySyncPayload;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.sync.ArmorVisibilitySyncManager;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.c2s.request.BoatPassengerSettingsRequestPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.network.s2c.sync.BoatPassengerSettingsSyncPayload;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;
import com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request.OpenHeadShulkerPayload;
import com.alonie.advancedaccessorysystem.feature.headshulker.network.c2s.request.OpenHeadShulkerRequestHandler;
import com.alonie.advancedaccessorysystem.feature.ride.client.sync.RideStateSyncClient;
import com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request.DismountPassengersPayload;
import com.alonie.advancedaccessorysystem.feature.ride.network.c2s.request.DismountPassengersRequestHandler;
import com.alonie.advancedaccessorysystem.feature.ride.network.s2c.sync.RideStateSyncPayload;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Networking bridge backed by Architectury raw API (NeoForge compatible).
 * Uses Identifier + RegistryFriendlyByteBuf instead of CustomPayload.
 */
public final class PlatformNetworking {
    private PlatformNetworking() {
    }

    public static void register() {
        // -- C2S: Armor Visibility Update --
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ArmorVisibilityUpdatePayload.ID,
                (buf, ctx) -> {
                    ArmorVisibilityUpdatePayload p = ArmorVisibilityUpdatePayload.read(buf);
                    ctx.queue(() -> ArmorVisibilitySyncManager.handleUpdate((ServerPlayer) ctx.getPlayer(), p));
                });

        // -- S2C: Armor Visibility Sync --
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ArmorVisibilitySyncPayload.ID,
                (buf, ctx) -> {
                    ArmorVisibilitySyncPayload p = ArmorVisibilitySyncPayload.read(buf);
                    ctx.queue(() -> ArmorVisibilitySyncClient.handleSync(p));
                });

        // -- C2S: Boat Passenger Settings Request --
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BoatPassengerSettingsRequestPayload.ID,
                (buf, ctx) -> {
                    BoatPassengerSettingsRequestPayload p = BoatPassengerSettingsRequestPayload.read(buf);
                    ctx.queue(() -> BoatPassengerSettingsSyncManager.handleRequest((ServerPlayer) ctx.getPlayer(), p));
                });

        // -- S2C: Boat Passenger Settings Sync --
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BoatPassengerSettingsSyncPayload.ID,
                (buf, ctx) -> {
                    BoatPassengerSettingsSyncPayload p = BoatPassengerSettingsSyncPayload.read(buf);
                    ctx.queue(() -> BoatPassengerSettingsSyncClient.handleSync(p));
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

        // -- S2C: Ride State Sync --
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, RideStateSyncPayload.ID,
                (buf, ctx) -> {
                    RideStateSyncPayload p = RideStateSyncPayload.read(buf);
                    ctx.queue(() -> RideStateSyncClient.handleSync(p));
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
