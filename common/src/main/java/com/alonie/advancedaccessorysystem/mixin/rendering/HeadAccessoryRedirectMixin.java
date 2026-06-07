package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.capability.HeadAccessoryCapabilities;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Redirects the HEAD equipment slot lookup during render state extraction
 * so that items from ANY accessory provider (vanilla head, Trinkets hat,
 * future providers) are visible on the player model using the vanilla
 * rendering pipeline.
 *
 * <p>Targets the second {@code getItemBySlot(HEAD)} call in
 * {@code LivingEntityRenderer.extractRenderState(LivingEntity, LivingEntityRenderState, float)}
 * (ordinal = 1), which is the main rendering path for non-armor head items
 * (skulls, pumpkins, banners, saddles, boats, etc.). The first call
 * (ordinal = 0) is the dragon-head bounding-box check and is left
 * untouched.
 *
 * <p>{@code headEquipment} (handled by {@code HumanoidArmorLayer}) is
 * intentionally not redirected. If a vanilla helmet is worn, it renders
 * via the armor layer while the accessory renders via {@code CustomHeadLayer}
 * on top — both visible independently.
 */
@Mixin(net.minecraft.client.renderer.entity.LivingEntityRenderer.class)
public class HeadAccessoryRedirectMixin {

    @Redirect(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
               + "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot"
                   + "(Lnet/minecraft/world/entity/EquipmentSlot;)"
                   + "Lnet/minecraft/world/item/ItemStack;",
            ordinal = 1
        )
    )
    private ItemStack aas$redirectHeadItem(LivingEntity entity, EquipmentSlot slot) {
        // Only intercept HEAD slot
        if (slot != EquipmentSlot.HEAD) {
            return entity.getItemBySlot(slot);
        }

        // Search all accessory providers for a custom accessory item.
        // An item qualifies if it has any HeadAccessoryCapability
        // (BOAT_PASSENGER, PLAYER_RIDE, HEAD_SHULKER_STORAGE).
        ItemStack accessory = AccessorySlotRegistry.findFirst(entity,
                stack -> !HeadAccessoryCapabilities.resolve(stack).isEmpty());

        if (!accessory.isEmpty()) {
            return accessory;
        }

        // No accessory found — return the vanilla head slot item
        return entity.getItemBySlot(slot);
    }
}
