package com.alonie.advancedaccessorysystem.mixin.ride;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerEntity.class)
public interface PlayerEntityCriticalHitInvoker {
    @Invoker("isCriticalHit")
    boolean advancedaccessorysystem$invokeIsCriticalHit(Entity target);

    @Invoker("cannotAttack")
    boolean advancedaccessorysystem$invokeCannotAttack(Entity target);
}
