package btw.lowercase.optiboxes.mixins;

import btw.lowercase.optiboxes.skybox.OptiFineSkybox;
import btw.lowercase.optiboxes.skybox.SkyboxManager;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SkyRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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

    @Shadow
    @Final
    private Minecraft minecraft;

    // TODO/NOTE: Had to use this, because I couldn't access the tickDelta value from outside the lambda
    @Unique
    private float getTickDelta() {
        DeltaTracker deltaTracker = this.minecraft.getDeltaTracker();
        if (this.level == null) {
            return deltaTracker.getGameTimeDeltaPartialTick(false);
        } else {
            return deltaTracker.getGameTimeDeltaPartialTick(!this.level.tickRateManager().runsNormally());
        }
    }

    @WrapOperation(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderEndSky()V"))
    private void optiboxes$renderEndSkybox(SkyRenderer instance, Operation<Void> original) {
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        boolean isEnabled = SkyboxManager.INSTANCE.isEnabled(this.level);
        original.call(instance);
        if (isEnabled) {
            PoseStack poseStack = new PoseStack();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            for (OptiFineSkybox optiFineSkybox : activeSkyboxes) {
                SkyboxManager.INSTANCE.getOptiFineSkyRenderer().renderSkybox(optiFineSkybox, poseStack, Objects.requireNonNull(this.level), 0.0F);
            }
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
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        boolean isEnabled = SkyboxManager.INSTANCE.isEnabled(this.level);
        if (isEnabled) {
            ClientLevel clientLevel = Objects.requireNonNull(this.level);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            for (OptiFineSkybox optiFineSkybox : activeSkyboxes) {
                SkyboxManager.INSTANCE.getOptiFineSkyRenderer().renderSkybox(optiFineSkybox, poseStack, clientLevel, getTickDelta());
            }
            poseStack.popPose();
        }

        original.call(instance, poseStack, bufferSource, timeOfDay, moonPhases, rainLevel, starBrightness);
    }

    @WrapWithCondition(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private boolean optiboxes$moveEndBatch(MultiBufferSource.BufferSource instance) {
        return !SkyboxManager.INSTANCE.isEnabled(this.level);
    }
}
