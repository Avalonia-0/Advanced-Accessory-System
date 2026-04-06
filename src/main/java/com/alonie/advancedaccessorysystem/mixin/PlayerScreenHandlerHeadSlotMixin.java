package com.alonie.advancedaccessorysystem.mixin;

import com.alonie.advancedaccessorysystem.AllItemsHeadEquippablePatch;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerHeadSlotMixin {
    @Inject(method = "canInsertIntoSlot", at = @At("HEAD"), cancellable = true)
    private void AdvancedAccessorySystem$allowCustomHeadItems(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty() || slot == null) {
            return;
        }

        Identifier background = slot.getBackgroundSprite();

        if (!PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE.equals(background)) {
            return;
        }

        if (AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(stack.getItem())) {
            cir.setReturnValue(true);
        } else {
        }
    }
}