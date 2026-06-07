package com.alonie.advancedaccessorysystem.feature.accessory.client.render;

import com.alonie.advancedaccessorysystem.mixin.rendering.LivingEntityRenderStateBlockMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renders {@link BlockState block items} on the player's head as 3D block
 * models instead of flat item sprites.
 *
 * <p>When a {@code BlockItem} is placed in the Trinkets hat slot (or any
 * non-vanilla head slot), this layer renders its block model using
 * {@code BlockRenderDispatcher.renderSingleBlock()} with the same head
 * positioning as the vanilla {@code CustomHeadLayer}.
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
        BlockState blockState = ((LivingEntityRenderStateBlockMixin) (Object) state).aas$headBlock;
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

        // Render the block model
        Minecraft mc = Minecraft.getInstance();
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        mc.getBlockRenderer().renderSingleBlock(
                blockState,
                poseStack,
                mc.renderBuffers().bufferSource(),
                packedLight,
                overlay
        );

        poseStack.popPose();
    }
}
