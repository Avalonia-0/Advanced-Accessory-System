package com.alonie.advancedaccessorysystem.compat.impl.cosmeticarmor;

import com.alonie.advancedaccessorysystem.compat.api.HeadAccessoryCompatBridge;
import com.alonie.advancedaccessorysystem.compat.api.HeadAccessorySlotRef;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public final class CosmeticArmorHeadAccessoryBridge implements HeadAccessoryCompatBridge {
    @Override
    public void initCommon() {
        CosmeticArmorCompat.initCommon();
    }

    @Override
    public void initClient() {
        CosmeticArmorCompat.initClient();
    }

    @Override
    public boolean isAvailable() {
        return CosmeticArmorCompat.isAvailable();
    }

    @Override
    public HeadAccessorySlotRef getCosmeticHeadSlotRef(LivingEntity entity) {
        CosmeticArmorCompat.CosmeticHeadSlotRef ref = CosmeticArmorCompat.getCosmeticHeadSlotRef(entity);
        if (ref == null) {
            return null;
        }
        return new HeadAccessorySlotRef(ref.inventory(), ref.slotIndex(), ref.stack());
    }

    @Override
    public ItemStack getCosmeticHeadStack(LivingEntity entity) {
        return CosmeticArmorCompat.getCosmeticHeadStack(entity);
    }

    @Override
    public boolean clearCosmeticHeadStack(LivingEntity entity, Predicate<ItemStack> predicate) {
        return CosmeticArmorCompat.clearCosmeticHeadStack(entity, predicate);
    }
}
