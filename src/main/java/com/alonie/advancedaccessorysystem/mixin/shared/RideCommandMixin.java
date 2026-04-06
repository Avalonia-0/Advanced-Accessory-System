package com.alonie.advancedaccessorysystem.mixin.shared;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.RideCommand;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RideCommand.class)
public class RideCommandMixin {
    @Redirect(
            method = "method_48082(Lnet/minecraft/class_2168;Lnet/minecraft/class_1297;Lnet/minecraft/class_1297;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_1297;method_5864()Lnet/minecraft/class_1299;"
            )
    )
    private static EntityType<?> advancedaccessorysystem$rideExtension(Entity entity) {
        return entity instanceof PlayerEntity ? null : entity.getType();
    }
}
