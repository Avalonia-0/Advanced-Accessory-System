package com.alonie.advancedaccessorysystem.mixin.ride;

import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerPassengerDismountMixin {
    @Shadow
    @Final
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerInput", at = @At("HEAD"))
    private void advancedaccessorysystem$applyPassengerSelfDismountCooldown(
            PlayerInputC2SPacket packet,
            CallbackInfo ci
    ) {
        if (packet.input().sneak()) {
            PlayerRideSyncManager.onPassengerRequestedSelfDismount(this.player);
        }
    }
}
