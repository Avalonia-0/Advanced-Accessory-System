package com.alonie.advancedaccessorysystem.mixin.ride;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows player entities to pass the vanilla rideability gate inside Entity.
 * If this redirect stops matching, player-to-player mounting will fail even
 * when the rest of the ride logic is still present.
 */
@Mixin(Entity.class)
public class EntityRidePlayerMixin {
    @Redirect(
            method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"
            )
    )
    private boolean advancedaccessorysystem$allowRidingPlayers(EntityType<?> instance) {
        return instance == EntityType.PLAYER || instance.canSerialize();
    }
}
