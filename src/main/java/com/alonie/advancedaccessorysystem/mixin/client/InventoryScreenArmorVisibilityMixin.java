package com.alonie.advancedaccessorysystem.mixin.client;

import com.alonie.advancedaccessorysystem.client.ArmorSlotVisibilityState;
import com.alonie.advancedaccessorysystem.client.gui.ArmorVisibilityButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenArmorVisibilityMixin {
    @Unique
    private final Map<EquipmentSlot, ButtonWidget> advancedAccessorySystem$armorButtons = new EnumMap<>(EquipmentSlot.class);

    @Inject(method = "init", at = @At("TAIL"))
    private void advancedAccessorySystem$initArmorVisibilityButtons(CallbackInfo ci) {
        this.advancedAccessorySystem$rebuildArmorVisibilityButtons();
        this.advancedAccessorySystem$updateArmorVisibilityButtonPositions();
    }

    @Inject(method = "onRecipeBookToggled", at = @At("TAIL"))
    private void advancedAccessorySystem$rebuildAfterRecipeBookToggle(CallbackInfo ci) {
        this.advancedAccessorySystem$rebuildArmorVisibilityButtons();
        this.advancedAccessorySystem$updateArmorVisibilityButtonPositions();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void advancedAccessorySystem$updateButtonsBeforeRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.advancedAccessorySystem$updateArmorVisibilityButtonPositions();
    }

    @Unique
    private void advancedAccessorySystem$rebuildArmorVisibilityButtons() {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (!ArmorSlotVisibilityState.isSupported(equipmentSlot)) {
                continue;
            }

            this.advancedAccessorySystem$armorButtons.computeIfAbsent(
                    equipmentSlot,
                    slot -> ((ScreenInvoker) this).advancedAccessorySystem$invokeAddDrawableChild(
                            ArmorVisibilityButton.create(slot, this::advancedAccessorySystem$updateArmorVisibilityButtonPositions)
                    )
            );
        }

        for (ButtonWidget button : this.advancedAccessorySystem$armorButtons.values()) {
            button.visible = false;
            button.active = false;
        }
    }

    @Unique
    private void advancedAccessorySystem$updateArmorVisibilityButtonPositions() {
        for (ButtonWidget button : this.advancedAccessorySystem$armorButtons.values()) {
            button.visible = false;
            button.active = false;
        }

        HandledScreenAccessor handledScreenAccessor = (HandledScreenAccessor) this;
        ScreenHandler handler = handledScreenAccessor.advancedAccessorySystem$getHandler();
        int x = handledScreenAccessor.advancedAccessorySystem$getX();
        int y = handledScreenAccessor.advancedAccessorySystem$getY();

        for (Slot slot : handler.slots) {
            EquipmentSlot equipmentSlot = this.advancedAccessorySystem$getVanillaArmorEquipmentSlot(handler, slot);
            if (!ArmorSlotVisibilityState.isSupported(equipmentSlot)) {
                continue;
            }

            ButtonWidget button = this.advancedAccessorySystem$armorButtons.get(equipmentSlot);
            if (button == null) {
                continue;
            }

            int buttonX = x + slot.x - ArmorVisibilityButton.SIZE - 2;
            int buttonY = y + slot.y + (18 - ArmorVisibilityButton.SIZE) / 2 - 1;
            button.setDimensionsAndPosition(ArmorVisibilityButton.SIZE, ArmorVisibilityButton.SIZE, buttonX, buttonY);
            ArmorVisibilityButton.refresh(button, equipmentSlot);
            button.visible = true;
            button.active = true;
        }
    }

    @Unique
    private EquipmentSlot advancedAccessorySystem$getVanillaArmorEquipmentSlot(ScreenHandler handler, Slot slot) {
        if (slot == null || !(handler instanceof PlayerScreenHandler)) {
            return null;
        }

        if (slot.id < PlayerScreenHandler.EQUIPMENT_START || slot.id >= PlayerScreenHandler.EQUIPMENT_END) {
            return null;
        }

        Identifier background = slot.getBackgroundSprite();
        if (PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE.equals(background)) {
            return EquipmentSlot.HEAD;
        }
        if (PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE.equals(background)) {
            return EquipmentSlot.CHEST;
        }
        if (PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE.equals(background)) {
            return EquipmentSlot.LEGS;
        }
        if (PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE.equals(background)) {
            return EquipmentSlot.FEET;
        }

        return null;
    }
}
