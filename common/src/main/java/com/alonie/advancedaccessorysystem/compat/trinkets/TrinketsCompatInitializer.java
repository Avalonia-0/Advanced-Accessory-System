package com.alonie.advancedaccessorysystem.compat.trinkets;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;
import com.alonie.advancedaccessorysystem.feature.headslot.rule.AllItemsHeadEquippablePatch;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Initializes Trinkets integration.
 *
 * <p>Registers the {@link ReflectionTrinketsHatBridge} for hat-stack queries
 * (used by {@code RideAccessoryHelper} / {@code ShulkerBoxCompat}) and
 * registers a trinket predicate so our head-slot items can enter the Trinkets
 * {@code head/hat} slot.
 *
 * <p><b>Slot predicate injection</b> is handled at data-pack level via
 * {@code data/trinkets/slots/head/hat.json}. Trinkets' {@code SlotLoader}
 * uses {@code findAllResources} which returns all resources for the same
 * path from all mods and data packs, then merges them (default
 * {@code replace: false}). This means our predicate ID is added to the
 * slot's {@code validator_predicates} set at resource-load time —
 * before any server→client sync — with zero runtime reflection.
 *
 * <p>All access is via reflection — no compile dependency on Trinkets.
 * Safe to call on both Fabric and NeoForge; absent Trinkets → no-op.
 */
public final class TrinketsCompatInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/Trinkets");
    private static boolean initialized = false;

    private static final Identifier PREDICATE_ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "head_slot_compat");
    private static final Identifier COSMETIC_PREDICATE_ID = Identifier.fromNamespaceAndPath(
            AdvancedAccessorySystemMod.MOD_ID, "head_cosmetic");

    private TrinketsCompatInitializer() {
    }

    public static void init() {
        if (initialized) return;
        initialized = true;

        try {
            Class.forName("dev.emi.trinkets.api.TrinketsApi");
        } catch (ClassNotFoundException e) {
            return;
        }

        // 1. Bridge for hat-stack queries (always available)
        TrinketsHatBridgeImpl.register(new ReflectionTrinketsHatBridge());

        // 2. Register predicate function globally.
        //    The predicate ID is added to the head/hat slot's validator_predicates
        //    set via data/trinkets/slots/head/hat.json (merged by SlotLoader).
        registerPredicate();

        // 3. Register predicate for cosmetic slot (head/cosmetic).
        //    Items that naturally equip to HEAD (helmets, skulls, pumpkins)
        //    can enter this slot for purely cosmetic display.
        registerCosmeticPredicate();
    }

    // ---- predicate registration -------------------------------------------

    private static void registerPredicate() {
        try {
            Class<?> apiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Class<?> triStateClass = Class.forName("net.fabricmc.fabric.api.util.TriState");
            Object triStateDefault = triStateClass.getField("DEFAULT").get(null);
            Object triStateTrue = triStateClass.getField("TRUE").get(null);

            Class<?> function3Class = Class.forName("com.mojang.datafixers.util.Function3");
            Method registerMethod = apiClass.getMethod(
                    "registerTrinketPredicate", Identifier.class, function3Class);

            // Pre-cache SlotReference.getId() Method — used inside the predicate
            Class<?> slotRefClass = Class.forName("dev.emi.trinkets.api.SlotReference");
            Method slotRefGetId = slotRefClass.getMethod("getId");

            InvocationHandler handler = (_proxy, method, args) -> {
                if (!"apply".equals(method.getName())) {
                    Class<?> retType = method.getReturnType();
                    if (retType == boolean.class) return false;
                    if (retType == int.class) return 0;
                    return null;
                }
                net.minecraft.world.item.ItemStack stack = (net.minecraft.world.item.ItemStack) args[0];
                Object slotRef = args[1];
                String slotId = (String) slotRefGetId.invoke(slotRef);
                if (slotId == null || !slotId.startsWith("head/hat")) {
                    return triStateDefault;
                }
                if (AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(stack.getItem())) {
                    return triStateTrue;
                }
                return triStateDefault;
            };

            Object proxy = Proxy.newProxyInstance(
                    function3Class.getClassLoader(),
                    new Class[]{function3Class},
                    handler);

            registerMethod.invoke(null, PREDICATE_ID, proxy);
            LOGGER.info("Registered Trinkets predicate {}", PREDICATE_ID);
        } catch (Exception e) {
            LOGGER.error("Failed to register Trinkets predicate", e);
        }
    }

    // ---- cosmetic slot predicate ------------------------------------------

    /**
     * Registers the {@code head_cosmetic} predicate that allows items
     * naturally equippable to the HEAD slot (helmets, skulls, pumpkins)
     * to enter the {@code head/cosmetic} Trinkets slot for display.
     *
     * <p>Custom accessories (boats, saddles, shulker boxes) have
     * {@code getPreferredEquipmentSlot} returning MAINHAND/OFFHAND,
     * so they are automatically excluded from this slot.
     */
    private static void registerCosmeticPredicate() {
        try {
            Class<?> apiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Class<?> triStateClass = Class.forName("net.fabricmc.fabric.api.util.TriState");
            Object triStateDefault = triStateClass.getField("DEFAULT").get(null);
            Object triStateTrue = triStateClass.getField("TRUE").get(null);

            Class<?> function3Class = Class.forName("com.mojang.datafixers.util.Function3");
            Method registerMethod = apiClass.getMethod(
                    "registerTrinketPredicate", Identifier.class, function3Class);

            InvocationHandler handler = (_proxy, method, args) -> {
                if (!"apply".equals(method.getName())) {
                    Class<?> retType = method.getReturnType();
                    if (retType == boolean.class) return false;
                    if (retType == int.class) return 0;
                    return null;
                }
                net.minecraft.world.item.ItemStack stack = (net.minecraft.world.item.ItemStack) args[0];
                // Check if the item naturally equips to HEAD via EQUIPPABLE component.
                // Custom accessories (boat/saddle/shulker) don't have EQUIPPABLE
                // for HEAD, so they're excluded automatically.
                net.minecraft.world.item.equipment.Equippable equippable =
                        stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
                if (equippable != null
                        && equippable.slot() == net.minecraft.world.entity.EquipmentSlot.HEAD) {
                    return triStateTrue;
                }
                return triStateDefault;
            };

            Object proxy = Proxy.newProxyInstance(
                    function3Class.getClassLoader(),
                    new Class[]{function3Class},
                    handler);

            registerMethod.invoke(null, COSMETIC_PREDICATE_ID, proxy);
            LOGGER.info("Registered Trinkets cosmetic predicate {}", COSMETIC_PREDICATE_ID);
        } catch (Exception e) {
            LOGGER.error("Failed to register Trinkets cosmetic predicate", e);
        }
    }
}
