package btw.lowercase.optiboxes.mixins;

import btw.lowercase.optiboxes.config.OptiBoxesConfig;
import btw.lowercase.optiboxes.skybox.SkyboxManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class MixinLevelRenderer {
    @Shadow
    @Final
    private SkyRenderer skyRenderer;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(method = "method_62215", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFog(Lnet/minecraft/client/renderer/FogParameters;)V", shift = At.Shift.AFTER), cancellable = true)
    private void optiboxes$renderSkyboxes(FogParameters fogParameters, DimensionSpecialEffects.SkyType skyType, float tickDelta, DimensionSpecialEffects dimensionSpecialEffects, CallbackInfo ci) {
        SkyboxManager skyboxManager = SkyboxManager.INSTANCE;
        if (OptiBoxesConfig.instance().enabled && !skyboxManager.getActiveSkyboxes().isEmpty()) {
            skyboxManager.renderSkyboxes(
                    this.skyRenderer,
                    new PoseStack(),
                    tickDelta,
                    Minecraft.getInstance().gameRenderer.getMainCamera(),
                    this.renderBuffers.bufferSource(),
                    fogParameters
            );
            ci.cancel();
        }
    }
}
