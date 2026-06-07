package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
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
 * <p>This ensures items placed in non-vanilla slots (Trinkets hat slot)
 * are rendered on the player model using the standard head-item pipeline.
 *
 * <p>Items with an {@code EQUIPPABLE} component targeting the HEAD slot
 * (helmets, skulls, pumpkins) are skipped — they are handled by
 * {@code CosmeticHelmetMixin} which injects into {@code headEquipment}
 * for armor-model rendering via {@code HumanoidArmorLayer}.
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

        // Skip items that naturally equip to HEAD (helmets, skulls, pumpkins).
        // These are handled by CosmeticHelmetMixin which renders them as
        // armor models via HumanoidArmorLayer (headEquipment).
        Equippable equippable = accessory.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == EquipmentSlot.HEAD) {
            return;
        }

        // Reference-identity check: if the found item IS the vanilla head
        // equipment object, then the vanilla pipeline already handles it.
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
