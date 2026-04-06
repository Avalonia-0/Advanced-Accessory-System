package com.alonie.advancedaccessorysystem.mixin;

import com.alonie.advancedaccessorysystem.AllItemsHeadEquippablePatch;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerHeadWearClickMixin {
    @Shadow @Final public DefaultedList<Slot> slots;
    @Shadow public abstract ItemStack getCursorStack();
    @Shadow public abstract void setCursorStack(ItemStack stack);
    @Shadow public abstract void sendContentUpdates();

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void AdvancedAccessorySystem$handleCustomHeadWear(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!(((Object) this) instanceof PlayerScreenHandler)) {
            return;
        }

        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return;
        }

        if (actionType != SlotActionType.PICKUP) {
            return;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.isEnabled()) {
            return;
        }

        Identifier background = slot.getBackgroundSprite();
        if (!PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE.equals(background)) {
            return;
        }

        ItemStack cursorStack = this.getCursorStack();
        if (cursorStack == null || cursorStack.isEmpty()) {
            // Only handling "place onto helmet slot" for now.
            return;
        }

        if (!AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(cursorStack.getItem())) {
            return;
        }

        if (!slot.canInsert(cursorStack)) {
            // We intentionally bypass vanilla insert restriction for custom head-wear items.
        }

        if (!slot.canTakeItems(player) && slot.hasStack()) {
            return;
        }

        ItemStack previous = slot.getStack().copy();
        ItemStack toWear = cursorStack.copy();
        toWear.setCount(1);

        slot.setStack(toWear);
        slot.markDirty();

        if (cursorStack.getCount() <= 1) {
            this.setCursorStack(previous);
        } else {
            ItemStack remaining = cursorStack.copy();
            remaining.decrement(1);
            if (previous.isEmpty()) {
                this.setCursorStack(remaining);
            } else {
                // Avoid complex partial-stack merge logic. Only swap cleanly when cursor held exactly one item.
                slot.setStack(previous);
                slot.markDirty();
                return;
            }
        }

        this.sendContentUpdates();
        ci.cancel();
    }
}