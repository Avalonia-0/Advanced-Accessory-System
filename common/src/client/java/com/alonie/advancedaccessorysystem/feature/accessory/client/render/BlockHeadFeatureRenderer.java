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
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renders {@link BlockState block items} on the player's head as 3D block
 * models using the {@link SubmitNodeCollector#submitBlock} API, which
 * integrates correctly with the 1.21.11 rendering pipeline.
 *
 * <p>When a {@code BlockItem} is placed in any accessory slot, this layer
 * renders its block model with the same head positioning as
 * {@code CustomHeadLayer}.
 */
public class BlockHeadFeatureRenderer
        extends RenderLayer<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> {

    public BlockHeadFeatureRenderer(
            RenderLayerParent<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       LivingEntityRenderState state, float limbSwing, float limbSwingAmount) {
        BlockState blockState = ((BlockHeadRenderState) state).aas$getHeadBlock();
        if (blockState == null) {
            return;
        }

        poseStack.pushPose();

        // Apply head transform — same chain as CustomHeadLayer
        EntityModel<LivingEntityRenderState> model = getParentModel();
        model.root().translateAndRotate(poseStack);
        if (model instanceof HeadedModel headedModel) {
            headedModel.translateToHead(poseStack);
        }
        CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);

        // Scale block to fit on head (block = 1m³, head ≈ 0.5m wide)
        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, -0.5, -0.5);

        // Submit the block model through the proper rendering pipeline.
        // submitBlock(poseStack, state, packedLight, overlay, tintARGB)
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        collector.submitBlock(poseStack, blockState, packedLight, overlay, -1);

        poseStack.popPose();
    }
}
