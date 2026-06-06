package com.alonie.advancedaccessorysystem.feature.accessory.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

/**
 * Renders custom head accessories (boat hats, saddle hats, shulker box hats)
 * on the player model using vanilla-style head item positioning.
 *
 * <p>Registered as a {@link RenderLayer} on {@code AvatarRenderer} so it
 * renders for all player entities regardless of which accessory slot provider
 * (vanilla head or Trinkets hat) holds the item.
 *
 * <p>Only items with non-empty {@code HeadAccessoryCapabilities} are rendered;
 * vanilla helmets and skulls pass through their own render layers
 * ({@code HumanoidArmorLayer}, {@code CustomHeadLayer}).
 */
public class HeadAccessoryFeatureRenderer
        extends RenderLayer<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> {

    public HeadAccessoryFeatureRenderer(
            RenderLayerParent<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       LivingEntityRenderState state, float limbSwing, float limbSwingAmount) {
        ItemStackRenderState accessoryItem =
                ((AccessoryRenderState) state).aas$getAccessoryItem();
        if (accessoryItem.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        EntityModel<LivingEntityRenderState> model = getParentModel();
        model.root().translateAndRotate(poseStack);
        if (model instanceof HeadedModel headedModel) {
            headedModel.translateToHead(poseStack);
        }
        CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);

        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        accessoryItem.submit(poseStack, collector, packedLight, overlay, state.outlineColor);

        poseStack.popPose();
    }
}
