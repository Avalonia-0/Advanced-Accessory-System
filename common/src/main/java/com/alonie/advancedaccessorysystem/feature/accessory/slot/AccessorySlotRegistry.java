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

    // ---- Rendering query ---------------------------------------------------

    /**
     * Return the first non-empty stack from a provider that
     * {@link AccessorySlotProvider#providesRender() provides rendering}.
     * Returns {@link ItemStack#EMPTY} if no visible accessory is equipped.
     */
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
