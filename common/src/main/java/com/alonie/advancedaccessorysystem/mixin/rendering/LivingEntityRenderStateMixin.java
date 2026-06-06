package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.client.render.AccessoryRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects an {@code accessoryItem} field into {@link LivingEntityRenderState}
 * so the accessory render layer can submit the pre-resolved item model during
 * {@code submit()} without needing access to the original entity.
 *
 * <p>This follows the same pattern as Trinkets'
 * {@code LivingEntityStateRenderMixin} which stores trinket pairs on the
 * render state.
 */
@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements AccessoryRenderState {

    @Unique
    public final ItemStackRenderState aas$accessoryItem = new ItemStackRenderState();

    @Override
    public ItemStackRenderState aas$getAccessoryItem() {
        return this.aas$accessoryItem;
    }
}
