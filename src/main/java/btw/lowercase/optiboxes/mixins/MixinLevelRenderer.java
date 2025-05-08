package btw.lowercase.optiboxes.mixins;

import btw.lowercase.optiboxes.skybox.OptiFineSkyRenderer;
import btw.lowercase.optiboxes.skybox.OptiFineSkybox;
import btw.lowercase.optiboxes.skybox.SkyboxManager;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SkyRenderer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class MixinLevelRenderer {
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Nullable
    private ClientLevel level;

    @Unique
    private float optiboxes$tickDelta;

    @Inject(method = "addSkyPass", at = @At("HEAD"))
    private void optiboxes$getLocals(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        this.optiboxes$tickDelta = tickDelta;
    }

    @WrapOperation(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderEndSky()V"))
    private void optiboxes$renderEndSkybox(SkyRenderer instance, Operation<Void> original) {
        original.call(instance);
        if (SkyboxManager.INSTANCE.isEnabled(this.level)) {
            List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
            ClientLevel clientLevel = Objects.requireNonNull(this.level);
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.rotate(Axis.YP.rotationDegrees(-90.0F));
            for (OptiFineSkybox optiFineSkybox : activeSkyboxes) {
                OptiFineSkyRenderer.INSTANCE.renderSkybox(optiFineSkybox, modelViewStack, clientLevel, 0.0F);
            }
            modelViewStack.popMatrix();
        }
    }

    @WrapOperation(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FI)V"))
    private void optiboxes$endBatchSunrise(SkyRenderer instance, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float sunAngle, int sunriseOrSunsetColor, Operation<Void> original) {
        original.call(instance, poseStack, bufferSource, sunAngle, sunriseOrSunsetColor);
        if (SkyboxManager.INSTANCE.isEnabled(this.level)) {
            renderBuffers.bufferSource().endBatch();
        }
    }

    @WrapOperation(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FIFF)V"))
    private void optiboxes$renderSkyboxes(SkyRenderer instance, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float timeOfDay, int moonPhases, float rainLevel, float starBrightness, Operation<Void> original) {
        if (SkyboxManager.INSTANCE.isEnabled(this.level)) {
            List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
            ClientLevel clientLevel = Objects.requireNonNull(this.level);
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.rotate(Axis.YP.rotationDegrees(-90.0F));
            for (OptiFineSkybox optiFineSkybox : activeSkyboxes) {
                OptiFineSkyRenderer.INSTANCE.renderSkybox(optiFineSkybox, modelViewStack, clientLevel, this.optiboxes$tickDelta);
            }
            modelViewStack.popMatrix();
        }

        original.call(instance, poseStack, bufferSource, timeOfDay, moonPhases, rainLevel, starBrightness);
    }

    @WrapWithCondition(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private boolean optiboxes$moveEndBatch(MultiBufferSource.BufferSource instance) {
        return !SkyboxManager.INSTANCE.isEnabled(this.level);
    }
}
