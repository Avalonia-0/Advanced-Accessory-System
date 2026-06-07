package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects ANY non-empty item from any accessory provider into
 * {@code LivingEntityRenderState.headItem} during render state extraction,
 * so the vanilla {@code CustomHeadLayer} renders it on the player's head.
 *
 * <p>This ensures items placed in non-vanilla slots (Trinkets hat slot,
 * future cosmetic slots, etc.) are rendered on the player model using
 * the standard head-item pipeline — exactly as if they were in the
 * vanilla head equipment slot.
 *
 * <p>Items already in the vanilla head slot are skipped because they
 * are already handled by the vanilla pipeline (via {@code headItem},
 * {@code HumanoidArmorLayer}, or {@code CustomHeadLayer}).
 * The reference-identity check {@code accessory == entity.getItemBySlot(HEAD)}
 * prevents double-rendering: if the item found by the registry is the
 * same object as the vanilla head equipment, vanilla already handles it.
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
        // If vanilla already set headItem for a non-armor head item
        // (pumpkin, skull, etc.), keep it — no override.
        if (!state.headItem.isEmpty()) {
            return;
        }

        // Find ANY non-empty item across all accessory providers
        ItemStack accessory = AccessorySlotRegistry.findFirst(entity,
                stack -> !stack.isEmpty());
        if (accessory.isEmpty()) {
            return;
        }

        // Reference-identity check: if the found item IS the vanilla head
        // equipment object, then the vanilla pipeline already handles it
        // (via headEquipment for armor or headItem for non-armor).
        // Only inject items from OTHER providers (Trinkets, etc.).
        if (accessory == entity.getItemBySlot(EquipmentSlot.HEAD)) {
            return;
        }

        // Resolve the accessory as a HEAD display item so CustomHeadLayer
        // renders it with the standard head-item pipeline.
        this.itemModelResolver.updateForLiving(
                state.headItem,
                accessory,
                ItemDisplayContext.HEAD,
                entity
        );
    }
}
