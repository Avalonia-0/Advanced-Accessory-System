package com.alonie.advancedaccessorysystem.mixin.ride;

import com.alonie.advancedaccessorysystem.feature.ride.rules.RideCombatHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Trims the lower part of a rider's hitbox after vanilla computes it.
 * Fabric events do not expose a post-bounding-box hook, so this mixin is the
 * stable place where ride combat/collision rules can adjust the result.
 */
@Mixin(Entity.class)
public abstract class EntityPassengerHitboxMixin {
    @Inject(
            method = "calculateBoundingBox()Lnet/minecraft/util/math/Box;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void advancedaccessorysystem$trimPassengerBoundingBox(CallbackInfoReturnable<Box> cir) {
        Entity self = (Entity) (Object) this;
        cir.setReturnValue(RideCombatHelper.getPassengerCollisionBox(self, cir.getReturnValue()));
    }
}
