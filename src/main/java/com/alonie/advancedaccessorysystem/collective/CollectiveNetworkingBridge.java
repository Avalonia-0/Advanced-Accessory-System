package com.alonie.advancedaccessorysystem.collective;

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
import com.natamus.collective_common_fabric.implementations.networking.api.Dispatcher;
import com.natamus.collective_common_fabric.implementations.networking.api.Network;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class CollectiveNetworkingBridge {
    private CollectiveNetworkingBridge() {
    }

    public static void register() {
        registerPacket(
                ArmorVisibilityUpdatePayload.ID,
                ArmorVisibilityUpdatePayload.class,
                ArmorVisibilityUpdatePayload::write,
                ArmorVisibilityUpdatePayload::read,
                message -> CollectiveTaskBridge.runServer(asServerPlayer(message.sender()), () ->
                        ArmorVisibilitySyncManager.handleUpdate(asServerPlayer(message.sender()), message.message()))
        );
        registerPacket(
                ArmorVisibilitySyncPayload.ID,
                ArmorVisibilitySyncPayload.class,
                ArmorVisibilitySyncPayload::write,
                ArmorVisibilitySyncPayload::read,
                message -> CollectiveTaskBridge.runClient(() ->
                        ArmorVisibilitySyncClient.handleSync(message.message()))
        );
        registerPacket(
                BoatPassengerSettingsRequestPayload.ID,
                BoatPassengerSettingsRequestPayload.class,
                BoatPassengerSettingsRequestPayload::write,
                BoatPassengerSettingsRequestPayload::read,
                message -> CollectiveTaskBridge.runServer(asServerPlayer(message.sender()), () ->
                        BoatPassengerSettingsSyncManager.handleRequest(asServerPlayer(message.sender()), message.message()))
        );
        registerPacket(
                BoatPassengerSettingsSyncPayload.ID,
                BoatPassengerSettingsSyncPayload.class,
                BoatPassengerSettingsSyncPayload::write,
                BoatPassengerSettingsSyncPayload::read,
                message -> CollectiveTaskBridge.runClient(() ->
                        BoatPassengerSettingsSyncClient.handleSync(message.message()))
        );
        registerPacket(
                OpenHeadShulkerPayload.ID,
                OpenHeadShulkerPayload.class,
                OpenHeadShulkerPayload::write,
                OpenHeadShulkerPayload::read,
                message -> CollectiveTaskBridge.runServer(asServerPlayer(message.sender()), () ->
                        OpenHeadShulkerRequestHandler.handleRequest(asServerPlayer(message.sender()), message.message()))
        );
        registerPacket(
                DismountPassengersPayload.ID,
                DismountPassengersPayload.class,
                DismountPassengersPayload::write,
                DismountPassengersPayload::read,
                message -> CollectiveTaskBridge.runServer(asServerPlayer(message.sender()), () ->
                        DismountPassengersRequestHandler.handleRequest(asServerPlayer(message.sender()), message.message()))
        );
        registerPacket(
                RideStateSyncPayload.ID,
                RideStateSyncPayload.class,
                RideStateSyncPayload::write,
                RideStateSyncPayload::read,
                message -> CollectiveTaskBridge.runClient(() ->
                        RideStateSyncClient.handleSync(message.message()))
        );
    }

    public static <T> void sendToServer(T message) {
        Dispatcher.sendToServer(message);
    }

    public static <T> void sendToClient(T message, ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        Dispatcher.sendToClient(message, player);
    }

    public static <T> void sendToAllClients(T message, MinecraftServer server) {
        if (server == null) {
            return;
        }
        Dispatcher.sendToAllClients(message, server);
    }

    private static <T> void registerPacket(
            Identifier id,
            Class<T> type,
            BiConsumer<T, PacketByteBuf> encoder,
            Function<PacketByteBuf, T> decoder,
            Consumer<com.natamus.collective_common_fabric.implementations.networking.data.PacketContext<T>> handler
    ) {
        Network.registerPacket(id, type, encoder, decoder, handler);
    }

    private static ServerPlayerEntity asServerPlayer(net.minecraft.entity.player.PlayerEntity player) {
        return player instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null;
    }
}
