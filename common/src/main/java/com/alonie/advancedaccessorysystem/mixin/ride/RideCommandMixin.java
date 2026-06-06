package com.alonie.advancedaccessorysystem.mixin.ride;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.commands.RideCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Extends the vanilla /ride command so player targets are accepted.
 * This is intentionally isolated from the higher-level interaction logic
 * because command validation happens entirely inside vanilla command code.
 */
@Mixin(RideCommand.class)
public class RideCommandMixin {
    @Redirect(
            method = "mount(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private static EntityType<?> advancedaccessorysystem$rideExtension(Entity entity) {
        EntityType<?> entityType = entity.getType();
        return entityType == EntityType.PLAYER ? null : entityType;
    }
}
