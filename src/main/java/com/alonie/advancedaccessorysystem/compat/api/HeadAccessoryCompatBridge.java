package com.alonie.advancedaccessorysystem.compat.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public interface HeadAccessoryCompatBridge {
    void initCommon();

    void initClient();

    boolean isAvailable();

    HeadAccessorySlotRef getCosmeticHeadSlotRef(LivingEntity entity);

    ItemStack getCosmeticHeadStack(LivingEntity entity);

    boolean clearCosmeticHeadStack(LivingEntity entity, Predicate<ItemStack> predicate);
}
