package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorSlotVisibilityState;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorVisibilityClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Connects the armor visibility system to the vanilla rendering pipeline.
 *
 * <p>Uses {@link ArmorSlotVisibilityState#getLocalMask()} for the local player
 * (which reads config directly) and {@link ArmorVisibilityClientCache#getSyncedMask}
 * for remote players (network-synced mask).
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
               + "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void aas$suppressHeadArmorIfHidden(LivingEntity entity, LivingEntityRenderState state,
                                                float partialTick, CallbackInfo ci) {
        if (!(state instanceof HumanoidRenderState humanoidState)) return;

        Minecraft client = Minecraft.getInstance();
        int mask;
        if (entity == client.player) {
            mask = ArmorSlotVisibilityState.getLocalMask();
        } else {
            mask = ArmorVisibilityClientCache.getSyncedMask(entity.getUUID());
        }

        if (ArmorVisibilityMask.isHidden(mask, EquipmentSlot.HEAD)) {
            humanoidState.headEquipment = net.minecraft.world.item.ItemStack.EMPTY;
        }
        if (ArmorVisibilityMask.isHidden(mask, EquipmentSlot.CHEST)) {
            humanoidState.chestEquipment = net.minecraft.world.item.ItemStack.EMPTY;
        }
        if (ArmorVisibilityMask.isHidden(mask, EquipmentSlot.LEGS)) {
            humanoidState.legsEquipment = net.minecraft.world.item.ItemStack.EMPTY;
        }
        if (ArmorVisibilityMask.isHidden(mask, EquipmentSlot.FEET)) {
            humanoidState.feetEquipment = net.minecraft.world.item.ItemStack.EMPTY;
        }
    }
}
