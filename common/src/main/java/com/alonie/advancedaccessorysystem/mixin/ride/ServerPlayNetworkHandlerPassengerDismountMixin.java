package com.alonie.advancedaccessorysystem.mixin.ride;

import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Watches sneak input on the server so passenger self-dismount cooldown is
 * applied at the same time vanilla receives the intent to leave the vehicle.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerPassengerDismountMixin {
    @Shadow
    @Final
    public ServerPlayer player;

    @Inject(method = "handlePlayerInput", at = @At("HEAD"))
    private void advancedaccessorysystem$applyPassengerSelfDismountCooldown(
            ServerboundPlayerInputPacket packet,
            CallbackInfo ci
    ) {
        if (packet.input().shift()) {
            PlayerRideSyncManager.onPassengerRequestedSelfDismount(this.player);
        }
    }
}
