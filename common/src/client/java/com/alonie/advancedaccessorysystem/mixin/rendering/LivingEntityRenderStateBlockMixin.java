package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.client.render.BlockHeadRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateBlockMixin implements BlockHeadRenderState {

    @Unique
    private BlockState aas$headBlock;

    @Override
    public BlockState aas$getHeadBlock() {
        return this.aas$headBlock;
    }

    @Override
    public void aas$setHeadBlock(BlockState blockState) {
        this.aas$headBlock = blockState;
    }
}
