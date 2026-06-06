package com.alonie.advancedaccessorysystem.compat.trinkets;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Bridge that accesses the Trinkets API via reflection so Trinkets and its
 * transitive dependencies are not needed at compile time.
 * Works on both Fabric and NeoForge.
 *
 * <p>All {@link Method} handles are cached after the first successful
 * resolution so repeated calls on tick-based hot paths incur zero
 * reflection lookup overhead.
 */
public class ReflectionTrinketsHatBridge implements TrinketsHatBridge {

    // ---- cached reflection handles (resolved lazily, cached forever) -----

    private static volatile Method cachedGetTrinketComponent;
    private static volatile Method cachedGetInventory;
    private static volatile Method cachedGetItem;
    private static volatile Method cachedGetContainerSize;
    private static volatile Method cachedSetItem;

    // ---- inventory access -------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> getInventory(LivingEntity entity) {
        try {
            if (cachedGetTrinketComponent == null) {
                Class<?> apiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
                cachedGetTrinketComponent = apiClass.getMethod("getTrinketComponent", LivingEntity.class);
            }
            java.util.Optional<?> opt = (java.util.Optional<?>)
                    cachedGetTrinketComponent.invoke(null, entity);
            if (opt.isPresent()) {
                Object component = opt.get();
                if (cachedGetInventory == null) {
                    cachedGetInventory = component.getClass().getMethod("getInventory");
                }
                return (Map<String, Map<String, Object>>) cachedGetInventory.invoke(component);
            }
        } catch (Exception ignored) {
        }
        return Map.of();
    }

    private Object getHatInventory(LivingEntity entity) {
        Map<String, Map<String, Object>> inv = getInventory(entity);
        Map<String, Object> headInv = inv.get("head");
        if (headInv == null) return null;
        return headInv.get("hat");
    }

    // ---- TrinketsHatBridge implementation ---------------------------------

    @Override
    public ItemStack getHatStack(LivingEntity entity) {
        Object hatInv = getHatInventory(entity);
        if (hatInv == null) return ItemStack.EMPTY;
        try {
            if (cachedGetItem == null) {
                cachedGetItem = hatInv.getClass().getMethod("getItem", int.class);
            }
            return (ItemStack) cachedGetItem.invoke(hatInv, 0);
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setHatStack(LivingEntity entity, ItemStack stack) {
        Object hatInv = getHatInventory(entity);
        if (hatInv == null) return;
        try {
            if (cachedSetItem == null) {
                cachedSetItem = hatInv.getClass().getMethod("setItem", int.class, ItemStack.class);
            }
            cachedSetItem.invoke(hatInv, 0, stack);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean clearHatStack(LivingEntity entity, Predicate<ItemStack> predicate) {
        Object hatInv = getHatInventory(entity);
        if (hatInv == null) return false;
        try {
            if (cachedGetContainerSize == null) {
                cachedGetContainerSize = hatInv.getClass().getMethod("getContainerSize");
            }
            if (cachedGetItem == null) {
                cachedGetItem = hatInv.getClass().getMethod("getItem", int.class);
            }
            if (cachedSetItem == null) {
                cachedSetItem = hatInv.getClass().getMethod("setItem", int.class, ItemStack.class);
            }
            int size = (int) cachedGetContainerSize.invoke(hatInv);
            for (int i = 0; i < size; i++) {
                ItemStack stack = (ItemStack) cachedGetItem.invoke(hatInv, i);
                if (predicate.test(stack)) {
                    cachedSetItem.invoke(hatInv, i, ItemStack.EMPTY);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
