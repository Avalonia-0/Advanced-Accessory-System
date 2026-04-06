package com.alonie.advancedaccessorysystem.mixin.shared;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityRidePlayerMixin {
    @Redirect(
            method = "method_5873(Lnet/minecraft/class_1297;ZZ)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_1299;method_5893()Z"
            )
    )
    private boolean advancedaccessorysystem$allowRidingPlayers(EntityType<?> instance) {
        return instance == EntityType.PLAYER || instance.isSaveable();
    }
}
