package com.alonie.advancedaccessorysystem.compat.trinkets;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * <p>Exceptions are logged at WARN level so silent failures are detectable
 * in the game log.
 */
public class ReflectionTrinketsHatBridge implements TrinketsHatBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/TrinketsBridge");

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
                LOGGER.info("Cached TrinketsApi.getTrinketComponent method");
            }
            java.util.Optional<?> opt = (java.util.Optional<?>)
                    cachedGetTrinketComponent.invoke(null, entity);
            if (opt.isPresent()) {
                Object component = opt.get();
                if (cachedGetInventory == null) {
                    cachedGetInventory = component.getClass().getMethod("getInventory");
                    LOGGER.info("Cached TrinketComponent.getInventory method");
                }
                return (Map<String, Map<String, Object>>) cachedGetInventory.invoke(component);
            } else {
                LOGGER.warn("TrinketComponent not present for entity {}", entity);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get Trinket inventory: {}", e.getMessage());
            LOGGER.debug("Trinket inventory error details", e);
        }
        return Map.of();
    }

    private Object getHatInventory(LivingEntity entity) {
        Map<String, Map<String, Object>> inv = getInventory(entity);
        if (inv.isEmpty()) {
            LOGGER.debug("Trinket inventory empty (no groups)");
            return null;
        }
        Map<String, Object> headInv = inv.get("head");
        if (headInv == null) {
            LOGGER.debug("No 'head' group in Trinket inventory");
            return null;
        }
        Object hatInv = headInv.get("hat");
        if (hatInv == null) {
            LOGGER.debug("No 'hat' slot in 'head' group");
        }
        return hatInv;
    }

    // ---- TrinketsHatBridge implementation ---------------------------------

    @Override
    public ItemStack getHatStack(LivingEntity entity) {
        Object hatInv = getHatInventory(entity);
        if (hatInv == null) return ItemStack.EMPTY;
        try {
            if (cachedGetItem == null) {
                cachedGetItem = hatInv.getClass().getMethod("getItem", int.class);
                LOGGER.info("Cached TrinketInventory.getItem method");
            }
            ItemStack stack = (ItemStack) cachedGetItem.invoke(hatInv, 0);
            if (!stack.isEmpty()) {
                LOGGER.debug("Found item in Trinkets hat slot: {}", stack.getItem());
            }
            return stack;
        } catch (Exception e) {
            LOGGER.warn("Failed to get item from Trinkets hat slot: {}", e.getMessage());
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
                LOGGER.info("Cached TrinketInventory.setItem method");
            }
            cachedSetItem.invoke(hatInv, 0, stack);
        } catch (Exception e) {
            LOGGER.warn("Failed to set item in Trinkets hat slot: {}", e.getMessage());
        }
    }

    @Override
    public boolean clearHatStack(LivingEntity entity, Predicate<ItemStack> predicate) {
        Object hatInv = getHatInventory(entity);
        if (hatInv == null) return false;
        try {
            if (cachedGetContainerSize == null) {
                cachedGetContainerSize = hatInv.getClass().getMethod("getContainerSize");
                LOGGER.info("Cached TrinketInventory.getContainerSize method");
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
                    if (!stack.isEmpty()) {
                        LOGGER.info("Cleared item {} from Trinkets hat slot", stack.getItem());
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to clear Trinkets hat slot: {}", e.getMessage());
        }
        return false;
    }
}
