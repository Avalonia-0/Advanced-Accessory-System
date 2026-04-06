package com.alonie.advancedaccessorysystem;

import com.alonie.advancedaccessorysystem.client.AdvancedAccessorySystemClient;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Kept as a small shared rule holder. We no longer mutate EQUIPPABLE at init time.
 * Instead, a PlayerScreenHandler mixin allows manual insertion into the player's helmet slot.
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

        Identifier id = Registries.ITEM.getId(item);
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