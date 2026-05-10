package com.alonie.advancedaccessorysystem.mixin.client.armorvisibility;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.render.ArmorVisibilityRenderHelper;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorSlotVisibilityState;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes hidden armor stacks from the player render state before downstream
 * layers inspect those stacks. If this stops applying, client visuals can
 * diverge even if armor feature rendering is partially cancelled elsewhere.
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererArmorVisibilityMixin {
    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void advancedAccessorySystem$hideHiddenArmorFromRenderState(
            PlayerLikeEntity player,
            PlayerEntityRenderState state,
            float tickDelta,
            CallbackInfo ci
    ) {
        int visibilityMask = ArmorVisibilitySyncClient.getMask(player);

        if (this.advancedAccessorySystem$shouldHideArmorStack(state.equippedHeadStack, EquipmentSlot.HEAD, visibilityMask)) {
            state.equippedHeadStack = ItemStack.EMPTY;
            state.headItemRenderState.clear();
            state.headItemAnimationProgress = 0.0F;
            state.wearingSkullType = null;
            state.wearingSkullProfile = null;
        }

        if (this.advancedAccessorySystem$shouldHideArmorStack(state.equippedChestStack, EquipmentSlot.CHEST, visibilityMask)) {
            state.equippedChestStack = ItemStack.EMPTY;
        }

        if (this.advancedAccessorySystem$shouldHideArmorStack(state.equippedLegsStack, EquipmentSlot.LEGS, visibilityMask)) {
            state.equippedLegsStack = ItemStack.EMPTY;
        }

        if (this.advancedAccessorySystem$shouldHideArmorStack(state.equippedFeetStack, EquipmentSlot.FEET, visibilityMask)) {
            state.equippedFeetStack = ItemStack.EMPTY;
        }
    }

    private boolean advancedAccessorySystem$shouldHideArmorStack(ItemStack stack, EquipmentSlot slot, int visibilityMask) {
        return ArmorSlotVisibilityState.isHidden(visibilityMask, slot)
                && ArmorVisibilityRenderHelper.hasHideableArmorModel(stack, slot);
    }
}
