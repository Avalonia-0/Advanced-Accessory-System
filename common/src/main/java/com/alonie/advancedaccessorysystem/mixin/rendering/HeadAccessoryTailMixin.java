package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.capability.HeadAccessoryCapabilities;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Populates the render state's {@code headItem} with a custom accessory
 * from ANY accessory provider (vanilla head slot, Trinkets hat slot, etc.)
 * during render state extraction, so the vanilla {@code CustomHeadLayer}
 * renders it using the standard head-item pipeline.
 *
 * <p>This approach avoids using {@code @Redirect} (which would need a
 * refmap in production environments) and instead injects at TAIL to
 * overwrite the resolved {@code headItem} when a custom accessory is
 * found.
 */
@Mixin(LivingEntityRenderer.class)
public class HeadAccessoryTailMixin {

    @Shadow
    @Final
    protected ItemModelResolver itemModelResolver;

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
               + "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void aas$injectAccessoryIntoHeadItem(LivingEntity entity,
                                                  LivingEntityRenderState state,
                                                  float partialTick,
                                                  CallbackInfo ci) {
        // Find a custom accessory across all providers
        ItemStack accessory = AccessorySlotRegistry.findFirst(entity,
                stack -> !HeadAccessoryCapabilities.resolve(stack).isEmpty());
        if (accessory.isEmpty()) {
            return;
        }

        // If vanilla already set headItem for a non-armor head item
        // (pumpkin, skull etc.), we keep it — no override.
        if (!state.headItem.isEmpty()) {
            return;
        }

        // Resolve the accessory as a HEAD display item, replacing
        // whatever vanilla may have left empty. CustomHeadLayer
        // will pick this up automatically.
        this.itemModelResolver.updateForLiving(
                state.headItem,
                accessory,
                ItemDisplayContext.HEAD,
                entity
        );
    }
}
