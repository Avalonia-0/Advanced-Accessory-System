package com.alonie.advancedaccessorysystem.feature.accessory.slot;

import com.alonie.advancedaccessorysystem.compat.trinkets.TrinketsHatBridgeImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Wraps the Trinkets {@code head/hat} slot via {@link TrinketsHatBridgeImpl}.
 *
 * <p>On platforms where Trinkets is absent (NeoForge), all methods are
 * no-ops — {@link TrinketsHatBridgeImpl#isAvailable()} returns false,
 * so queries skip this provider transparently.
 *
 * <p>Provides rendering on the player model via the common
 * {@link HeadAccessoryFeatureRenderer}.
 */
public final class TrinketsHatSlotProvider implements AccessorySlotProvider {
    public static final String NAME = "trinkets_hat";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ItemStack getStack(LivingEntity entity) {
        if (!TrinketsHatBridgeImpl.isAvailable()) {
            return ItemStack.EMPTY;
        }
        return TrinketsHatBridgeImpl.get().getHatStack(entity);
    }

    @Override
    public boolean setStack(LivingEntity entity, ItemStack stack) {
        if (!TrinketsHatBridgeImpl.isAvailable()) {
            return false;
        }
        TrinketsHatBridgeImpl.get().setHatStack(entity, stack);
        return true;
    }

    @Override
    public boolean clearIf(LivingEntity entity, Predicate<ItemStack> predicate) {
        if (!TrinketsHatBridgeImpl.isAvailable()) {
            return false;
        }
        return TrinketsHatBridgeImpl.get().clearHatStack(entity, predicate);
    }

    @Override
    public boolean providesRender() {
        return TrinketsHatBridgeImpl.isAvailable();
    }
}
