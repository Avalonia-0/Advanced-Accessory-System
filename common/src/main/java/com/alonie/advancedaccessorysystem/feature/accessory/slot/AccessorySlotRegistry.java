package com.alonie.advancedaccessorysystem.feature.accessory.slot;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Static registry of {@link AccessorySlotProvider} instances.
 *
 * <p>Feature code queries this registry instead of checking individual
 * slots. Providers are queried in registration order — the first
 * match wins for read/clear operations.
 *
 * <p>Thread-safe: {@link CopyOnWriteArrayList} ensures safe iteration
 * during tick-based queries even if providers are registered late.
 */
public final class AccessorySlotRegistry {
    private static final List<AccessorySlotProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private AccessorySlotRegistry() {
    }

    // ---- Registration ------------------------------------------------------

    /** Register a slot provider. Called during mod bootstrap. */
    public static void register(AccessorySlotProvider provider) {
        Objects.requireNonNull(provider);
        PROVIDERS.add(provider);
    }

    /** Remove all providers. Called on server stop. */
    public static void reset() {
        PROVIDERS.clear();
    }

    // ---- Gameplay queries --------------------------------------------------

    /**
     * Find the first non-empty stack matching {@code predicate}, scanning
     * providers in registration order. Returns {@link ItemStack#EMPTY} if
     * no slot matches.
     */
    public static ItemStack findFirst(LivingEntity entity, Predicate<ItemStack> predicate) {
        for (AccessorySlotProvider provider : PROVIDERS) {
            ItemStack stack = provider.getStack(entity);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Like {@link #findFirst} but only scans the provider with the given
     * {@code providerName}. Returns {@link ItemStack#EMPTY} if the provider
     * is not found or has no matching stack.
     */
    public static ItemStack findFirst(LivingEntity entity, Predicate<ItemStack> predicate,
                                       String providerName) {
        for (AccessorySlotProvider provider : PROVIDERS) {
            if (!provider.name().equals(providerName)) continue;
            ItemStack stack = provider.getStack(entity);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return stack;
            }
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Like {@link #findFirst} but also returns which provider matched.
     * Useful when callers need to write back to the same slot
     * (e.g. shulker box sessions).
     */
    public static Optional<SlotQueryResult> findFirstWithSlot(
            LivingEntity entity, Predicate<ItemStack> predicate) {
        for (AccessorySlotProvider provider : PROVIDERS) {
            ItemStack stack = provider.getStack(entity);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return Optional.of(new SlotQueryResult(provider, stack));
            }
        }
        return Optional.empty();
    }

    /** Returns true if any provider has a non-empty stack matching the predicate. */
    public static boolean anyMatch(LivingEntity entity, Predicate<ItemStack> predicate) {
        return !findFirst(entity, predicate).isEmpty();
    }

    /**
     * Clear the first slot whose stack matches {@code predicate}.
     * Returns true if any slot was cleared.
     */
    public static boolean clearFirst(LivingEntity entity, Predicate<ItemStack> predicate) {
        for (AccessorySlotProvider provider : PROVIDERS) {
            if (provider.clearIf(entity, predicate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to equip an item stack to the first empty accessory slot.
     * Scans providers in registration order. Useful for auto-equip
     * interactions.
     *
     * @param entity the wearer
     * @param stack  the item to equip (not modified; use the return
     *               value to know if the caller should shrink the stack)
     * @return true if the item was placed into a slot
     */
    public static boolean tryEquip(LivingEntity entity, ItemStack stack) {
        Objects.requireNonNull(stack);
        for (AccessorySlotProvider provider : PROVIDERS) {
            if (provider.getStack(entity).isEmpty()) {
                return provider.setStack(entity, stack.copy());
            }
        }
        return false;
    }

    // ---- Rendering query ---------------------------------------------------

    /**
     * Return the first non-empty stack from a provider that
     * {@link AccessorySlotProvider#providesRender() provides rendering}.
     * Returns {@link ItemStack#EMPTY} if no visible accessory is equipped.
     *
     * @deprecated No longer needed — the {@code @Redirect} mixin in
     *             {@code HeadAccessoryRedirectMixin} handles rendering
     *             by injecting into the vanilla rendering pipeline directly.
     *             Scheduled for removal in the next major version.
     */
    @Deprecated(since = "1.2.0", forRemoval = false)
    public static ItemStack findRenderStack(LivingEntity entity) {
        for (AccessorySlotProvider provider : PROVIDERS) {
            if (provider.providesRender()) {
                ItemStack stack = provider.getStack(entity);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    // ---- Result type -------------------------------------------------------

    /** Result of {@link #findFirstWithSlot}: the matching provider and stack. */
    public record SlotQueryResult(AccessorySlotProvider provider, ItemStack stack) {
    }
}
