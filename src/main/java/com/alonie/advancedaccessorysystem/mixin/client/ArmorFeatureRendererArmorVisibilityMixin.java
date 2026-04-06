package com.alonie.advancedaccessorysystem.mixin.client;

import com.alonie.advancedaccessorysystem.client.ArmorSlotVisibilityState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (client.player == null) {
            return;
        }

        if (playerRenderState.id != client.player.getId()) {
            return;
        }

        if (!ArmorSlotVisibilityState.isHidden(slot)) {
            return;
        }

        ci.cancel();
    }
}
