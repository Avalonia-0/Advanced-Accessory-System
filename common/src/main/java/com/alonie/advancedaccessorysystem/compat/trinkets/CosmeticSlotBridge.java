package com.alonie.advancedaccessorysystem.compat.trinkets;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Reflection bridge for reading the Trinkets {@code head/cosmetic} slot.
 * Modeled after {@link ReflectionTrinketsHatBridge} but targets the
 * cosmetic slot (group = "head", name = "cosmetic") instead of
 * the hat slot (group = "head", name = "hat").
 *
 * <p>Uses {@link #findMethodByTypes} to resolve methods by parameter/return
 * types instead of method names, since Trinkets may use intermediary names.
 */
public class CosmeticSlotBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/CosmeticBridge");
    private static CosmeticSlotBridge instance;

    private volatile Method cachedGetTrinketComponent;
    private volatile Method cachedGetInventory;
    private volatile Method cachedGetItem;

    private CosmeticSlotBridge() {
    }

    public static CosmeticSlotBridge get() {
        if (instance == null) {
            instance = new CosmeticSlotBridge();
        }
        return instance;
    }

    // ---- helper: find method by parameter/return types (mapping-independent) ----

    private static Method findMethodByTypes(Class<?> clazz, Class<?> returnType, Class<?>... paramTypes) {
        for (Method m : clazz.getMethods()) {
            if (m.getReturnType() != returnType) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != paramTypes.length) continue;
            boolean match = true;
            for (int i = 0; i < params.length; i++) {
                if (params[i] != paramTypes[i]) {
                    match = false;
                    break;
                }
            }
            if (match) return m;
        }
        return null;
    }

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
        } catch (Exception e) {
            LOGGER.warn("Failed to get Trinket inventory for cosmetic: {}", e.getMessage());
        }
        return Map.of();
    }

    // ---- public API -------------------------------------------------------

    /**
     * Read the item in the Trinkets {@code head/cosmetic} slot.
     * Returns {@link ItemStack#EMPTY} if no item is present or if
     * Trinkets is unavailable.
     */
    public ItemStack getCosmeticStack(LivingEntity entity) {
        if (entity == null) return ItemStack.EMPTY;
        try {
            Map<String, Map<String, Object>> inv = getInventory(entity);
            if (inv.isEmpty()) return ItemStack.EMPTY;

            Map<String, Object> headGroup = inv.get("head");
            if (headGroup == null) return ItemStack.EMPTY;

            Object cosmeticInv = headGroup.get("cosmetic");
            if (cosmeticInv == null) return ItemStack.EMPTY;

            if (cachedGetItem == null) {
                cachedGetItem = findMethodByTypes(cosmeticInv.getClass(), ItemStack.class, int.class);
                if (cachedGetItem == null) {
                    LOGGER.error("Could not find getItem(int) on cosmetic inventory!");
                    return ItemStack.EMPTY;
                }
            }

            ItemStack stack = (ItemStack) cachedGetItem.invoke(cosmeticInv, 0);
            if (stack != null && !stack.isEmpty()) {
                LOGGER.debug("Found cosmetic head item: {}", stack.getItem());
            }
            return stack;
        } catch (Exception e) {
            LOGGER.warn("Failed to read cosmetic slot: {}", e.getMessage());
            return ItemStack.EMPTY;
        }
    }
}
