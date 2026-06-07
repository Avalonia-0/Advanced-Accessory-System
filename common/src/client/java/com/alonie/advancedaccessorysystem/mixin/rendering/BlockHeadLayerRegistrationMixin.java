package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.client.render.BlockHeadFeatureRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class BlockHeadLayerRegistrationMixin {

    @Shadow
    @SuppressWarnings("rawtypes")
    protected abstract boolean addLayer(RenderLayer layer);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void aas$addBlockHeadLayer(CallbackInfo ci) {
        if ((Object) this instanceof AvatarRenderer) {
            LivingEntityRenderer rawThis = (LivingEntityRenderer) (Object) this;
            this.addLayer(new BlockHeadFeatureRenderer(rawThis));
        }
    }
}
