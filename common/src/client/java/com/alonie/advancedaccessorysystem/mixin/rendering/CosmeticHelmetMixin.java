package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects into {@code HumanoidMobRenderer.getEquipmentIfRenderable()} to
 * replace the HEAD equipment with a HEAD-equippable item from any
 * accessory provider (Trinkets hat slot).
 *
 * <p>Items with an {@code EQUIPPABLE} component targeting the HEAD slot
 * (helmets, skulls, pumpkins) in the Trinkets hat slot will be rendered
 * as armor models via {@code HumanoidArmorLayer}, purely cosmetically.
 * The original head equipment's attributes continue to apply from the
 * vanilla armor slot.
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

        // Find a HEAD-equippable item from any accessory provider
        // (VanillaHeadSlotProvider, TrinketsHatSlotProvider, etc.).
        // Only items with EQUIPPABLE component for HEAD qualify.
        ItemStack cosmetic = AccessorySlotRegistry.findFirst(entity,
                stack -> {
                    Equippable e = stack.get(DataComponents.EQUIPPABLE);
                    return e != null && e.slot() == EquipmentSlot.HEAD;
                });

        if (!cosmetic.isEmpty()) {
            // Replace the rendered equipment with the cosmetic item.
            // HumanoidArmorLayer renders the cosmetic item's armor model
            // (or CustomHeadLayer for non-armor items like skulls).
            cir.setReturnValue(cosmetic.copy());
        }
    }
}
