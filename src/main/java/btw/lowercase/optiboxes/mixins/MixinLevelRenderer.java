package btw.lowercase.optiboxes.mixins;

import btw.lowercase.optiboxes.config.OptiBoxesConfig;
import btw.lowercase.optiboxes.skybox.OptiFineSkybox;
import btw.lowercase.optiboxes.skybox.SkyboxManager;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class MixinLevelRenderer {
    @Shadow
    @Final
    private SkyRenderer skyRenderer;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Nullable
    private ClientLevel level;

    @WrapOperation(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderEndSky()V"))
    private void optiboxes$renderEndSkybox(SkyRenderer instance, Operation<Void> original) {
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        if (OptiBoxesConfig.instance().enabled && !activeSkyboxes.isEmpty() && this.level != null) {
            PoseStack poseStack = new PoseStack();
            for (OptiFineSkybox optiFineSkybox : activeSkyboxes) {
                optiFineSkybox.renderEndSky(skyRenderer, poseStack, this.level);
            }
        } else {
            original.call(instance);
        }
    }

    @Inject(method = "method_62215", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;<init>()V", shift = At.Shift.BEFORE), cancellable = true)
    private void optiboxes$renderSkyboxes(FogParameters fogParameters, DimensionSpecialEffects.SkyType skyType, float tickDelta, DimensionSpecialEffects dimensionSpecialEffects, CallbackInfo ci) {
        List<OptiFineSkybox> activeSkyboxes = SkyboxManager.INSTANCE.getActiveSkyboxes();
        if (OptiBoxesConfig.instance().enabled
                && !activeSkyboxes.isEmpty()
                && this.level != null
                && this.level.effects().skyType() == DimensionSpecialEffects.SkyType.OVERWORLD) {
            PoseStack poseStack = new PoseStack();
            for (OptiFineSkybox optiFineSkybox : activeSkyboxes) {
                optiFineSkybox.renderOverworldSky(
                        this.skyRenderer,
                        poseStack,
                        tickDelta,
                        this.level,
                        Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(),
                        this.renderBuffers.bufferSource(),
                        fogParameters
                );
            }
            ci.cancel();
        }
    }

    @WrapWithCondition(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FIFFLnet/minecraft/client/renderer/FogParameters;)V"))
    private boolean optiboxes$toggleSunMoonStars(SkyRenderer instance, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float f, int i, float g, float h, FogParameters fogParameters) {
        return OptiBoxesConfig.instance().renderSunMoonStars;
    }
}
