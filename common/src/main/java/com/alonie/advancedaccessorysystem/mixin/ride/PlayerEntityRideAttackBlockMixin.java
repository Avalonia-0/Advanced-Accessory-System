package com.alonie.advancedaccessorysystem.mixin.ride;

import com.alonie.advancedaccessorysystem.feature.ride.rules.RideCombatHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents melee attacks between a rider and their vehicle player.
 * This rule is enforced at the attack entry point because there is no event
 * layer that can cancel the vanilla path early enough.
 */
@Mixin(Player.class)
public abstract class PlayerEntityRideAttackBlockMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void advancedaccessorysystem$blockRidePairAttacks(Entity target, CallbackInfo ci) {
        Player attacker = (Player) (Object) this;
        if (RideCombatHelper.isRidePair(attacker, target)) {
            ci.cancel();
        }
    }
}
