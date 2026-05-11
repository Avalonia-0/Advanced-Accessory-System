package com.alonie.advancedaccessorysystem.mixin.client.armorvisibility;

import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderManager.class)
public interface EntityRenderManagerAccessor {
    @Accessor("equipmentModelLoader")
    EquipmentModelLoader advancedAccessorySystem$getEquipmentModelLoader();
}
