package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.client.render.BlockHeadRenderState;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.VanillaHeadSlotProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects ANY non-empty custom accessory from any accessory provider into
 * {@code LivingEntityRenderState.headItem}, so the vanilla {@code CustomHeadLayer}
 * renders it on the player's head.
 *
 * <p>Custom accessories (boats, blocks, saddles, shulker boxes) ALWAYS show
 * regardless of what other slots contain, enabling multi-slot mixing:
 * a vanilla helmet renders via {@code headEquipment} while a Trinkets
 * boat renders via {@code headItem} — both visible simultaneously.
 *
 * <p>Providers are queried in priority order:
 * <ol>
 *   <li>Non-vanilla providers (Trinkets hat slot, etc.) — custom accessories</li>
 *   <li>Vanilla head slot — only if no non-vanilla item was found</li>
 * </ol>
 *
 * <p>Items with {@code EQUIPPABLE→HEAD} (helmets, skulls, pumpkins) are
 * skipped — they are handled by {@code CosmeticHelmetMixin} via
 * {@code headEquipment}.
 */
@Mixin(LivingEntityRenderer.class)
public class HeadAccessoryTailMixin {

    @Shadow
    @Final
    protected ItemModelResolver itemModelResolver;

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
               + "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void aas$injectAccessoryIntoHeadItem(LivingEntity entity,
                                                  LivingEntityRenderState state,
                                                  float partialTick,
                                                  CallbackInfo ci) {
        // If vanilla already set headItem for a non-armor head item
        // (pumpkin, skull, etc.), keep it — no override.
        if (!state.headItem.isEmpty()) {
            return;
        }

        // 1. Prefer custom accessories from non-vanilla providers.
        //    These always render regardless of vanilla head slot contents.
        ItemStack accessory = AccessorySlotRegistry.findFirst(entity,
                stack -> !stack.isEmpty(), VanillaHeadSlotProvider.NAME);

        // 2. Nothing from other providers — try vanilla head slot as fallback.
        if (accessory.isEmpty()) {
            accessory = AccessorySlotRegistry.findFirst(entity,
                    stack -> !stack.isEmpty());
            if (accessory == entity.getItemBySlot(EquipmentSlot.HEAD)) {
                return; // vanilla already handles it via headEquipment/headItem
            }
        }

        if (accessory.isEmpty()) {
            return;
        }

        // Skip items that naturally equip to HEAD (helmets, skulls, pumpkins).
        // These are handled by CosmeticHelmetMixin via headEquipment.
        Equippable equippable = accessory.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == EquipmentSlot.HEAD) {
            return;
        }

        // BlockItems render as 3D block models on the head.
        // Store the BlockState for the BlockHeadFeatureRenderer to pick up.
        if (accessory.getItem() instanceof BlockItem blockItem) {
            ((BlockHeadRenderState) state).aas$setHeadBlock(
                    blockItem.getBlock().defaultBlockState());
            return;
        }

        // Resolve the custom accessory as a HEAD display item.
        // CustomHeadLayer will render it alongside any armor models.
        this.itemModelResolver.updateForLiving(
                state.headItem,
                accessory,
                ItemDisplayContext.HEAD,
                entity
        );
    }
}
