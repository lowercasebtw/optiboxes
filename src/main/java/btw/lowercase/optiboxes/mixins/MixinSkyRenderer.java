package btw.lowercase.optiboxes.mixins;

import btw.lowercase.optiboxes.OptiBoxesClient;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public abstract class MixinSkyRenderer {
    @WrapWithCondition(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(FLnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean uniskies$toggleSun(SkyRenderer instance, float rainLevel, MultiBufferSource multiBufferSource, PoseStack poseStack) {
        return !OptiBoxesClient.getConfig().enabled.isEnabled() || OptiBoxesClient.getConfig().renderSunMoon.isEnabled();
    }

    @WrapWithCondition(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(IFLnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean uniskies$toggleMoon(SkyRenderer instance, int moonPhases, float rainLevel, MultiBufferSource multiBufferSource, PoseStack poseStack) {
        return !OptiBoxesClient.getConfig().enabled.isEnabled() || OptiBoxesClient.getConfig().renderSunMoon.isEnabled();
    }

    @WrapWithCondition(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private boolean uniskies$disableBatch(MultiBufferSource.BufferSource instance) {
        return !OptiBoxesClient.getConfig().enabled.isEnabled() || OptiBoxesClient.getConfig().renderSunMoon.isEnabled();
    }

    @WrapWithCondition(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(Lnet/minecraft/client/renderer/FogParameters;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean uniskies$toggleStars(SkyRenderer instance, FogParameters fogParameters, float starBrightness, PoseStack poseStack) {
        return !OptiBoxesClient.getConfig().enabled.isEnabled() || OptiBoxesClient.getConfig().renderSunMoon.isEnabled();
    }
}
