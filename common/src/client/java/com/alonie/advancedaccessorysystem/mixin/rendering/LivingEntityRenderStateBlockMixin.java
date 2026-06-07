package com.alonie.advancedaccessorysystem.mixin.rendering;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateBlockMixin {

    @Unique
    public BlockState aas$headBlock;
}
