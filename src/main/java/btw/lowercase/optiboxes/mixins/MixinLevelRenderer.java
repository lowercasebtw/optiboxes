package btw.lowercase.optiboxes.mixins;

import btw.lowercase.optiboxes.config.OptiBoxesConfig;
import btw.lowercase.optiboxes.skybox.OptiFineSkybox;
import btw.lowercase.optiboxes.skybox.SkyboxManager;
import btw.lowercase.optiboxes.utils.CommonUtils;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import org.jetbrains.annotations.Nullable;
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

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean isEnabled(List<OptiFineSkybox> activeSkyboxes) {
        return OptiBoxesConfig.instance().enabled && !activeSkyboxes.isEmpty() && this.level != null;
    }

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
        boolean isEnabled = isEnabled(activeSkyboxes);
        if (isEnabled) {
            CommonUtils.enableBlend();
            CommonUtils.depthMask(false);
        }

        original.call(instance);
        if (isEnabled) {
            PoseStack poseStack = new PoseStack();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            for (OptiFineSkybox optiFineSkybox : activeSkyboxes) {
                optiFineSkybox.render(poseStack, Objects.requireNonNull(this.level), 0.0F);
            }

            CommonUtils.depthMask(true);
            CommonUtils.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Inject(method = "method_62215", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;<init>()V", shift = At.Shift.AFTER))
    private void optiboxes$top(FogParameters fogParameters, DimensionSpecialEffects.SkyType skyType, float tickDelta, DimensionSpecialEffects dimensionSpecialEffects, CallbackInfo ci) {
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        if (isEnabled(activeSkyboxes)) {
            CommonUtils.enableBlend();
            CommonUtils.depthMask(false);
        }
    }

    @WrapOperation(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FI)V"))
    private void optiboxes$endBatchSunrise(SkyRenderer instance, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float sunAngle, int sunriseOrSunsetColor, Operation<Void> original) {
        original.call(instance, poseStack, bufferSource, sunAngle, sunriseOrSunsetColor);
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        if (isEnabled(activeSkyboxes)) {
            renderBuffers.bufferSource().endBatch();
        }
    }

    @WrapOperation(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FIFFLnet/minecraft/client/renderer/FogParameters;)V"))
    private void optiboxes$renderSkyboxes(SkyRenderer instance, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float timeOfDay, int moonPhases, float rainLevel, float starBrightness, FogParameters fogParameters, Operation<Void> original) {
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        boolean isEnabled = isEnabled(activeSkyboxes);
        if (isEnabled) {
            ClientLevel clientLevel = Objects.requireNonNull(this.level);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            CommonUtils.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ZERO);
            for (OptiFineSkybox optiFineSkybox : activeSkyboxes) {
                optiFineSkybox.render(poseStack, clientLevel, getTickDelta());
            }
            poseStack.popPose();
        }

        original.call(instance, poseStack, bufferSource, timeOfDay, moonPhases, rainLevel, starBrightness, fogParameters);
        if (isEnabled) {
            CommonUtils.disableBlend();
            CommonUtils.defaultBlendFunc();
        }
    }

    @WrapWithCondition(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private boolean optiboxes$moveEndBatch(MultiBufferSource.BufferSource instance) {
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        return !isEnabled(activeSkyboxes);
    }

    @Inject(method = "method_62215", at = @At("TAIL"))
    private void optiboxes$bottom(FogParameters fogParameters, DimensionSpecialEffects.SkyType skyType, float tickDelta, DimensionSpecialEffects dimensionSpecialEffects, CallbackInfo ci) {
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        if (isEnabled(activeSkyboxes) && Objects.requireNonNull(this.level).effects().skyType() != DimensionSpecialEffects.SkyType.END) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            CommonUtils.depthMask(true);
        }
    }
}
