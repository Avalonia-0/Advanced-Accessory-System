package com.alonie.advancedaccessorysystem.feature.headslot.rule;

import com.alonie.advancedaccessorysystem.AdvancedAccessorySystemMod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Kept as a small shared rule holder. We no longer mutate EQUIPPABLE at init time.
 * Instead, a InventoryMenu mixin allows manual insertion into the player's helmet slot.
 */
public final class AllItemsHeadEquippablePatch {
    private AllItemsHeadEquippablePatch() {
    }

    public static void register() {
    }

    public static boolean shouldAllowManualHeadInsert(Item item) {
        if (item == null || item == Items.AIR) {
            return false;
        }

        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) {
            return false;
        }

        String namespace = id.getNamespace();
        String path = id.getPath();

        // Avoid interfering with this mod's own custom armor pieces.
        if (AdvancedAccessorySystemMod.MOD_ID.equals(namespace)) {
            return false;
        }

        // Keep obvious player helmet/headwear items on vanilla logic only.
        if (path.contains("helmet") || path.contains("skull") || path.endsWith("_head") || path.contains("pumpkin")) {
            return false;
        }
        return true;
    }
}
