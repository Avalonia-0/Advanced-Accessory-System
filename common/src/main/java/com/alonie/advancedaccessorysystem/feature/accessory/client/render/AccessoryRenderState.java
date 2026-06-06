package com.alonie.advancedaccessorysystem.feature.accessory.client.render;

import net.minecraft.client.renderer.item.ItemStackRenderState;

/**
 * Type-safe accessor for the accessory item field injected into
 * {@link net.minecraft.client.renderer.entity.state.LivingEntityRenderState}
 * by {@code LivingEntityRenderStateMixin}.
 */
public interface AccessoryRenderState {
    ItemStackRenderState aas$getAccessoryItem();
}
