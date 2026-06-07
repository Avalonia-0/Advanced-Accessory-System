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
 * <p>Method lookups use parameter-based resolution instead of name-based
 * because the Trinkets jar may use intermediary mappings at runtime
 * (e.g. {@code method_5438} instead of {@code getItem}).
 */
public class ReflectionTrinketsHatBridge implements TrinketsHatBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/TrinketsBridge");

    // ---- cached reflection handles (resolved lazily, cached forever) -----

    private static volatile Method cachedGetTrinketComponent;
    private static volatile Method cachedGetInventory;
    private static volatile Method cachedGetItem;
    private static volatile Method cachedGetContainerSize;
    private static volatile Method cachedSetItem;

    // ---- helper: find method by parameter/return type, ignoring name -----

    /**
     * Finds a method on the given class that matches the parameter types
     * and return type. Unlike {@code getMethod(String, Class...)}, this
     * works regardless of whether the runtime uses Mojang or intermediary
     * method names.
     */
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
            if (match) {
                return m;
            }
        }
        return null;
    }

    // ---- inventory access -------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> getInventory(LivingEntity entity) {
        try {
            if (cachedGetTrinketComponent == null) {
                Class<?> apiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
                // TrinketsApi.getTrinketComponent(LivingEntity) -> Optional<TrinketComponent>
                cachedGetTrinketComponent = apiClass.getMethod("getTrinketComponent", LivingEntity.class);
                LOGGER.info("Cached TrinketsApi.getTrinketComponent method");
            }
            java.util.Optional<?> opt = (java.util.Optional<?>)
                    cachedGetTrinketComponent.invoke(null, entity);
            if (opt.isPresent()) {
                Object component = opt.get();
                if (cachedGetInventory == null) {
                    // TrinketComponent.getInventory() -> Map<String, Map<String, TrinketInventory>>
                    cachedGetInventory = component.getClass().getMethod("getInventory");
                    LOGGER.info("Cached TrinketComponent.getInventory method");
                }
                return (Map<String, Map<String, Object>>) cachedGetInventory.invoke(component);
            } else {
                LOGGER.warn("TrinketComponent not present for entity {}", entity);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get Trinket inventory: {}", e.getMessage());
        }
        return Map.of();
    }

    private Object getHatInventory(LivingEntity entity) {
        Map<String, Map<String, Object>> inv = getInventory(entity);
        if (inv.isEmpty()) {
            LOGGER.warn("Trinket inventory empty (no groups) for entity {}", entity);
            return null;
        }
        Map<String, Object> headInv = inv.get("head");
        if (headInv == null) {
            LOGGER.warn("No 'head' group in Trinket inventory. Available groups: {}", inv.keySet());
            return null;
        }
        Object hatInv = headInv.get("hat");
        if (hatInv == null) {
            LOGGER.warn("No 'hat' slot in 'head' group. Available slots: {}", headInv.keySet());
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
                cachedGetItem = findMethodByTypes(hatInv.getClass(), ItemStack.class, int.class);
                if (cachedGetItem == null) {
                    LOGGER.error("Could not find getItem(int) method on {}!", hatInv.getClass().getName());
                    return ItemStack.EMPTY;
                }
                LOGGER.info("Cached method: {}.{} (resolved by types)",
                        cachedGetItem.getDeclaringClass().getSimpleName(),
                        cachedGetItem.getName());
            }
            ItemStack stack = (ItemStack) cachedGetItem.invoke(hatInv, 0);
            if (!stack.isEmpty()) {
                LOGGER.info("Found item in Trinkets hat slot: {}", stack.getItem());
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
                cachedSetItem = findMethodByTypes(hatInv.getClass(), void.class, int.class, ItemStack.class);
                if (cachedSetItem == null) {
                    LOGGER.error("Could not find setItem(int,ItemStack) method on {}!",
                            hatInv.getClass().getName());
                    return;
                }
                LOGGER.info("Cached method: {}.{} (resolved by types)",
                        cachedSetItem.getDeclaringClass().getSimpleName(),
                        cachedSetItem.getName());
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
                cachedGetContainerSize = findMethodByTypes(hatInv.getClass(), int.class);
                if (cachedGetContainerSize == null) {
                    LOGGER.error("Could not find getContainerSize() method on {}!",
                            hatInv.getClass().getName());
                    return false;
                }
                LOGGER.info("Cached method: {}.{} (resolved by types)",
                        cachedGetContainerSize.getDeclaringClass().getSimpleName(),
                        cachedGetContainerSize.getName());
            }
            if (cachedGetItem == null) {
                cachedGetItem = findMethodByTypes(hatInv.getClass(), ItemStack.class, int.class);
                if (cachedGetItem == null) {
                    LOGGER.error("Could not find getItem(int) method!");
                    return false;
                }
            }
            if (cachedSetItem == null) {
                cachedSetItem = findMethodByTypes(hatInv.getClass(), void.class, int.class, ItemStack.class);
                if (cachedSetItem == null) {
                    LOGGER.error("Could not find setItem(int,ItemStack) method!");
                    return false;
                }
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
