package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.capability.HeadAccessoryCapabilities;
import com.alonie.advancedaccessorysystem.feature.accessory.client.render.AccessoryRenderState;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Populates the {@code accessoryItem} render-state field during
 * {@code extractRenderState} so the {@code HeadAccessoryFeatureRenderer}
 * can submit the pre-resolved item model during {@code submit()}.
 *
 * <p>Only extracts items with custom accessory capabilities
 * ({@code BOAT_PASSENGER}, {@code PLAYER_RIDE}, {@code HEAD_SHULKER_STORAGE})
 * to avoid double-rendering vanilla helmets and skulls.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererExtractMixin {

    @Shadow
    @Final
    protected ItemModelResolver itemModelResolver;

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
               + "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void aas$extractAccessory(LivingEntity entity, LivingEntityRenderState state,
                                       float partialTick, CallbackInfo ci) {
        AccessoryRenderState accState = (AccessoryRenderState) state;
        var accessoryItem = accState.aas$getAccessoryItem();

        // Find the first item with custom accessory capabilities across all providers
        ItemStack accessoryStack = AccessorySlotRegistry.findFirst(entity,
                stack -> !HeadAccessoryCapabilities.resolve(stack).isEmpty());

        // Skip if nothing found, or if vanilla already handles this via headItem
        if (accessoryStack.isEmpty() || !state.headItem.isEmpty()) {
            accessoryItem.clear();
            return;
        }

        this.itemModelResolver.updateForLiving(
                accessoryItem,
                accessoryStack,
                ItemDisplayContext.HEAD,
                entity
        );
    }
}
