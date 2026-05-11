package com.alonie.advancedaccessorysystem.mixin.ride;

import com.alonie.advancedaccessorysystem.feature.ride.logic.PlayerHeadAccessoryBreakManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the server-side attack path to trigger head accessory break effects
 * only when the hit satisfies the same critical-hit checks vanilla uses.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityCriticalBoatBreakMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void advancedaccessorysystem$breakTargetHeadBoatOnCriticalHit(Entity target, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.isSpectator()) {
            return;
        }

        PlayerEntityCriticalHitInvoker invoker = (PlayerEntityCriticalHitInvoker) player;
        if (invoker.advancedaccessorysystem$invokeCannotAttack(target)) {
            return;
        }

        if (player.getAttackCooldownProgress(0.5F) <= 0.9F) {
            return;
        }

        if (!invoker.advancedaccessorysystem$invokeIsCriticalHit(target)) {
            return;
        }

        PlayerHeadAccessoryBreakManager.tryBreakPlayerHeadAccessoryOnCritical(
                player,
                target
        );
    }
}
