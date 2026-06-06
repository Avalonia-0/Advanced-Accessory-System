package com.alonie.advancedaccessorysystem.mixin.ride;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerEntityCriticalHitInvoker {
    @Invoker("canCriticalAttack")
    boolean advancedaccessorysystem$invokeIsCriticalHit(Entity target);

    @Invoker("cannotAttack")
    boolean advancedaccessorysystem$invokeCannotAttack(Entity target);
}
