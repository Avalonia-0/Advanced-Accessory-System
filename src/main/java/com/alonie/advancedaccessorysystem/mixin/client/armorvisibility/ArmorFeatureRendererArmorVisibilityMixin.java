package com.alonie.advancedaccessorysystem.mixin.client.armorvisibility;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.render.ArmorVisibilityRenderHelper;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state.ArmorSlotVisibilityState;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.client.sync.ArmorVisibilitySyncClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels armor feature rendering when synced visibility state says the armor
 * model should be hidden. This is the last reliable renderer-level hook before
 * vanilla queues the model draw commands.
 */
@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererArmorVisibilityMixin {
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void AdvancedAccessorySystem$cancelHiddenArmorRendering(
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            ItemStack stack,
            EquipmentSlot slot,
            int light,
            BipedEntityRenderState state,
            CallbackInfo ci
    ) {
        if (!(state instanceof PlayerEntityRenderState playerRenderState)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        if (!(client.world.getEntityById(playerRenderState.id) instanceof PlayerLikeEntity player)) {
            return;
        }

        if (!ArmorVisibilityRenderHelper.hasHideableArmorModel(stack, slot)) {
            return;
        }

        if (!ArmorSlotVisibilityState.isHidden(ArmorVisibilitySyncClient.getMask(player), slot)) {
            return;
        }

        ci.cancel();
    }
}
