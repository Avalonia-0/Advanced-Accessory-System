package com.alonie.advancedaccessorysystem.mixin.headslot;

import com.alonie.advancedaccessorysystem.feature.headslot.rule.AllItemsHeadEquippablePatch;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extends the helmet-slot insertion gate so non-helmet items can enter the
 * player head slot when AAS allows them. Targets ArmorSlot (package-private)
 * which is the specific slot type used for armor pieces in the player inventory.
 */
@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class PlayerScreenHandlerHeadSlotMixin {
    @Shadow
    private EquipmentSlot slot;

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void advancedAccessorysystem$allowCustomHeadItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (this.slot != EquipmentSlot.HEAD) {
            return;
        }

        if (AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(stack.getItem())) {
            cir.setReturnValue(true);
        }
    }
}
