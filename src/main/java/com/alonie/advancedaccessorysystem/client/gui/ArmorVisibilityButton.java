package com.alonie.advancedaccessorysystem.client.gui;

import com.alonie.advancedaccessorysystem.client.ArmorSlotVisibilityState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;

public final class ArmorVisibilityButton extends ButtonWidget {
    public static final int SIZE = 13;

    private static final Identifier TEXTURE_ABLE = Identifier.of("advanced-accessory-system", "armor_visibility/able");
    private static final Identifier TEXTURE_ABLE_HIGHLIGHT = Identifier.of("advanced-accessory-system", "armor_visibility/able_highlight");
    private static final Identifier TEXTURE_DISABLE = Identifier.of("advanced-accessory-system", "armor_visibility/disable");
    private static final Identifier TEXTURE_DISABLE_HIGHLIGHT = Identifier.of("advanced-accessory-system", "armor_visibility/disable_highlight");

    private final EquipmentSlot slot;

    private ArmorVisibilityButton(EquipmentSlot slot, PressAction onPress) {
        super(0, 0, SIZE, SIZE, net.minecraft.text.Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.slot = slot;
    }

    public static ArmorVisibilityButton create(EquipmentSlot slot, Runnable postToggleAction) {
        ArmorVisibilityButton widget = new ArmorVisibilityButton(slot, button -> {
            ArmorSlotVisibilityState.toggle(slot);
            refresh((ArmorVisibilityButton) button);
            postToggleAction.run();
        });

        refresh(widget);
        return widget;
    }

    public static void refresh(ButtonWidget widget, EquipmentSlot slot) {
        if (widget instanceof ArmorVisibilityButton armorVisibilityButton) {
            refresh(armorVisibilityButton);
        }
    }

    private static void refresh(ArmorVisibilityButton widget) {
        widget.setMessage(net.minecraft.text.Text.literal(""));
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Identifier texture = this.advancedAccessorySystem$getTexture();
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.width, this.height);
    }

    private Identifier advancedAccessorySystem$getTexture() {
        boolean hidden = ArmorSlotVisibilityState.isHidden(this.slot);
        boolean highlighted = this.isSelected();

        if (hidden) {
            return highlighted ? TEXTURE_DISABLE_HIGHLIGHT : TEXTURE_DISABLE;
        }

        return highlighted ? TEXTURE_ABLE_HIGHLIGHT : TEXTURE_ABLE;
    }
}
