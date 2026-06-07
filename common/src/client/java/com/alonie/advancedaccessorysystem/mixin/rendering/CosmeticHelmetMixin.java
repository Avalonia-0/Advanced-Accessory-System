package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.cosmetic.slot.CosmeticSlotProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects into {@code HumanoidMobRenderer.getEquipmentIfRenderable()} to
 * replace the HEAD equipment with the item from the cosmetic slot.
 *
 * <p>This causes the vanilla {@code HumanoidArmorLayer} to render the
 * cosmetic item's armor model on the player's head, while the original
 * head equipment's attributes (if any) continue to apply.
 */
@Mixin(net.minecraft.client.renderer.entity.HumanoidMobRenderer.class)
public class CosmeticHelmetMixin {

    @Inject(
        method = "getEquipmentIfRenderable(Lnet/minecraft/world/entity/LivingEntity;"
               + "Lnet/minecraft/world/entity/EquipmentSlot;)"
               + "Lnet/minecraft/world/item/ItemStack;",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void aas$replaceHeadWithCosmetic(LivingEntity entity,
                                                     EquipmentSlot slot,
                                                     CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.HEAD) {
            return;
        }

        ItemStack cosmetic = CosmeticSlotProvider.getCosmeticHead(entity);
        if (!cosmetic.isEmpty()) {
            // Replace the rendered equipment with the cosmetic item.
            // HumanoidArmorLayer will render the cosmetic item's armor model
            // (or CustomHeadLayer for non-armor items like skulls/pumpkins).
            cir.setReturnValue(cosmetic.copy());
        }
    }
}
