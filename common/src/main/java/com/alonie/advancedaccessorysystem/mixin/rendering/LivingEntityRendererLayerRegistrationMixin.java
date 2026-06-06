package com.alonie.advancedaccessorysystem.mixin.rendering;

import com.alonie.advancedaccessorysystem.feature.accessory.client.render.HeadAccessoryFeatureRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers {@link HeadAccessoryFeatureRenderer} as a render layer on
 * {@code AvatarRenderer} (the player renderer) during construction.
 *
 * <p>Uses a mixin into the {@code LivingEntityRenderer} constructor rather
 * than platform-specific APIs so the layer is registered identically on
 * both Fabric and NeoForge without additional dependencies.
 *
 * <p>Raw types are used for the {@code addLayer} call because the
 * compile-time generic parameters of {@code LivingEntityRenderer} (which are
 * abstract) differ from the concrete types at each call site while the
 * bytecode erases to the same raw {@code RenderLayer} type.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererLayerRegistrationMixin {

    @Shadow
    @SuppressWarnings("rawtypes")
    protected abstract boolean addLayer(RenderLayer layer);

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "<init>", at = @At("TAIL"))
    private void aas$addHeadAccessoryLayer(CallbackInfo ci) {
        if ((Object) this instanceof AvatarRenderer) {
            LivingEntityRenderer rawThis = (LivingEntityRenderer) (Object) this;
            this.addLayer(new HeadAccessoryFeatureRenderer(rawThis));
        }
    }
}
