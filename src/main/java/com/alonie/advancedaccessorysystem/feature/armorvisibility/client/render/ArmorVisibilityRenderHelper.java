package com.alonie.advancedaccessorysystem.feature.armorvisibility.client.render;

import com.alonie.advancedaccessorysystem.mixin.client.armorvisibility.EntityRenderManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public final class ArmorVisibilityRenderHelper {
    private ArmorVisibilityRenderHelper() {
    }

    public static boolean hasHideableArmorModel(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) {
            return false;
        }

        EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);
        if (equippable == null || equippable.slot() != slot || equippable.assetId().isEmpty()) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return false;
        }

        EquipmentModelLoader loader = ((EntityRenderManagerAccessor) client.getEntityRenderDispatcher())
                .advancedAccessorySystem$getEquipmentModelLoader();
        EquipmentModel model = loader.get(equippable.assetId().orElseThrow());
        EquipmentModel.LayerType layerType = slot == EquipmentSlot.LEGS
                ? EquipmentModel.LayerType.HUMANOID_LEGGINGS
                : EquipmentModel.LayerType.HUMANOID;

        return !model.getLayers(layerType).isEmpty();
    }
}
