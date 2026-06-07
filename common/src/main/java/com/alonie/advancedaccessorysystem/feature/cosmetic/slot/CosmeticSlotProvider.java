package com.alonie.advancedaccessorysystem.feature.cosmetic.slot;

import com.alonie.advancedaccessorysystem.compat.trinkets.CosmeticSlotBridge;
import com.alonie.advancedaccessorysystem.compat.trinkets.TrinketsHatBridgeImpl;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotProvider;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * {@link AccessorySlotProvider} for the Trinkets {@code head/cosmetic} slot.
 *
 * <p>This slot holds items that are naturally HEAD-equippable (helmets,
 * pumpkins, skulls) for purely cosmetic display. Items placed here do
 * NOT provide their original attributes (armor protection, etc.).
 *
 * <p>The rendering is handled by {@code CosmeticHelmetMixin} which
 * injects into {@code HumanoidMobRenderer.getEquipmentIfRenderable()}
 * to swap the {@code headEquipment} render state with this slot's item.
 */
public class CosmeticSlotProvider implements AccessorySlotProvider {

    public static final String NAME = "cosmetic_head";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ItemStack getStack(LivingEntity entity) {
        if (entity == null || !(entity instanceof Player)) {
            return ItemStack.EMPTY;
        }
        if (!TrinketsHatBridgeImpl.isAvailable()) {
            return ItemStack.EMPTY;
        }
        return CosmeticSlotBridge.get().getCosmeticStack(entity);
    }

    @Override
    public boolean setStack(LivingEntity entity, ItemStack stack) {
        // For now, cosmetic slot is read-only from our abstraction.
        // Items are placed via the Trinkets GUI.
        return false;
    }

    @Override
    public boolean clearIf(LivingEntity entity, Predicate<ItemStack> predicate) {
        ItemStack current = getStack(entity);
        if (!current.isEmpty() && predicate.test(current)) {
            // Trinkets handles clearing via its own GUI
            return false;
        }
        return false;
    }

    @Override
    public boolean providesRender() {
        return false;
    }

    /**
     * Convenience method for {@link CosmeticHelmetMixin} to quickly
     * query the cosmetic slot without accessing the registry.
     */
    public static ItemStack getCosmeticHead(LivingEntity entity) {
        return AccessorySlotRegistry.findFirst(entity,
                stack -> !stack.isEmpty(), NAME);
    }
}
