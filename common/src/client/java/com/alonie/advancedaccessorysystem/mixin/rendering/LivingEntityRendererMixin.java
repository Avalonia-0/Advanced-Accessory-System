package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorSlotVisibilityState;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorVisibilityClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses armor rendering for hidden equipment slots.
 *
 * <p>Injects at TAIL of {@code HumanoidMobRenderer.extractHumanoidRenderState()},
 * which runs AFTER all four equipment slots ({@code headEquipment},
 * {@code chestEquipment}, {@code legsEquipment}, {@code feetEquipment}) have
 * been populated and AFTER {@code CosmeticHelmetMixin} has applied any
 * cosmetic replacements. This ensures invisible armor takes effect on the
 * final rendered state, regardless of which provider the equipment came from.
 *
 * <p>Only affects items with an {@code EQUIPPABLE} component (armor pieces).
 * Custom accessories (boats, saddles, shulker boxes) rendered via
 * {@code HeadAccessoryTailMixin} → {@code headItem} are not affected.
 */
@Mixin(HumanoidMobRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(
        method = "extractHumanoidRenderState(Lnet/minecraft/world/entity/LivingEntity;"
               + "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;F"
               + "Lnet/minecraft/client/renderer/item/ItemModelResolver;)V",
        at = @At("TAIL")
    )
    private static void aas$suppressHiddenArmor(LivingEntity entity,
                                                 HumanoidRenderState state,
                                                 float partialTick,
                                                 ItemModelResolver resolver,
                                                 CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        int mask;
        if (entity == client.player) {
            mask = ArmorSlotVisibilityState.getLocalMask();
        } else {
            mask = ArmorVisibilityClientCache.getSyncedMask(entity.getUUID());
        }

        if (ArmorVisibilityMask.isHidden(mask, EquipmentSlot.HEAD)) {
            state.headEquipment = net.minecraft.world.item.ItemStack.EMPTY;
        }
        if (ArmorVisibilityMask.isHidden(mask, EquipmentSlot.CHEST)) {
            state.chestEquipment = net.minecraft.world.item.ItemStack.EMPTY;
        }
        if (ArmorVisibilityMask.isHidden(mask, EquipmentSlot.LEGS)) {
            state.legsEquipment = net.minecraft.world.item.ItemStack.EMPTY;
        }
        if (ArmorVisibilityMask.isHidden(mask, EquipmentSlot.FEET)) {
            state.feetEquipment = net.minecraft.world.item.ItemStack.EMPTY;
        }
    }
}
