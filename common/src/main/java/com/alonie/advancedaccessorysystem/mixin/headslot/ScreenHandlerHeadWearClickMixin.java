package com.alonie.advancedaccessorysystem.mixin.headslot;

import com.alonie.advancedaccessorysystem.feature.headslot.rule.AllItemsHeadEquippablePatch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles manual pickup-place behavior for custom head-wear items so the head
 * slot behaves consistently even when vanilla cursor logic would reject them.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerHeadWearClickMixin {
    @Shadow @Final public NonNullList<Slot> slots;
    @Shadow public abstract ItemStack getCarried();
    @Shadow public abstract void setCarried(ItemStack stack);
    @Shadow public abstract void broadcastChanges();

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void AdvancedAccessorySystem$handleCustomHeadWear(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        if (!(((Object) this) instanceof InventoryMenu)) {
            return;
        }

        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return;
        }

        if (actionType != ClickType.PICKUP) {
            return;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.isActive()) {
            return;
        }

        Identifier background = slot.getNoItemIcon();
        if (!InventoryMenu.EMPTY_ARMOR_SLOT_HELMET.equals(background)) {
            return;
        }

        ItemStack cursorStack = this.getCarried();
        if (cursorStack == null || cursorStack.isEmpty()) {
            // Only handling "place onto helmet slot" for now.
            return;
        }

        if (!AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(cursorStack.getItem())) {
            return;
        }

        if (!slot.mayPlace(cursorStack)) {
            // We intentionally bypass vanilla insert restriction for custom head-wear items.
        }

        if (!slot.mayPickup(player) && slot.hasItem()) {
            return;
        }

        ItemStack previous = slot.getItem().copy();
        ItemStack toWear = cursorStack.copy();
        toWear.setCount(1);

        slot.set(toWear);
        slot.setChanged();

        if (cursorStack.getCount() <= 1) {
            this.setCarried(previous);
        } else {
            ItemStack remaining = cursorStack.copy();
            remaining.shrink(1);
            if (previous.isEmpty()) {
                this.setCarried(remaining);
            } else {
                // Avoid complex partial-stack merge logic. Only swap cleanly when cursor held exactly one item.
                slot.set(previous);
                slot.setChanged();
                return;
            }
        }

        this.broadcastChanges();
        ci.cancel();
    }
}
