/**
 * Skyboxify
 * A skybox mod that allows you to use OptiFine skies in Fabric 1.21+
 * <p>
 * Copyright (C) 2025-2026 lowercasebtw
 * Copyright (C) 2025-2026 Contributors to the project retain their copyright
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * "MINECRAFT" LINKING EXCEPTION TO THE GPL
 */

package btw.lowercase.skyboxify.mixins;

import btw.lowercase.skyboxify.Skyboxify;
import btw.lowercase.skyboxify.api.SkyboxifyImpl;
import btw.lowercase.skyboxify.events.SkyRenderEvent;
import btw.lowercase.skyboxify.skybox.renderer.SkyFeatureRenderer;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >=26.1 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//? } else {
/*import net.minecraft.client.Camera;
*///? }

//? >=1.21.11 {
import net.minecraft.world.level.dimension.DimensionType;
 //?} else {
/*import net.minecraft.client.renderer.DimensionSpecialEffects;
*///?}

//? >=1.21.6 {
//? >=26.3 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
//? } else {
/*import com.mojang.blaze3d.buffers.GpuBufferSlice;
*///? }
//? }

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class MixinLevelRenderer_SkyEvents {
    @Unique
    private static float skyboxify$tickDelta = 0.0F;

    @Unique
    private static SkyFeatureRenderer skyboxify$skyFeatureRenderer = null;

    @Inject(method = "addSkyPass", at = @At("HEAD"))
    private void skyboxify$getLocals(
            final FrameGraphBuilder frame,
            //? >=26.1 {
            final CameraRenderState cameraState,
            //? } else {
            /*final Camera camera,
            *///? }
            //? <=1.21.8
            //final float tickDelta,
            //? >=1.21.6 {
            final GpuBufferSlice skyFog,
            //?} else {
            /*final net.minecraft.client.renderer.FogParameters skyFog,
             *///?}
            CallbackInfo ci) {
        if (skyboxify$skyFeatureRenderer == null) {
            skyboxify$skyFeatureRenderer = new SkyFeatureRenderer(
                    //? >=26.2 {
                    Minecraft.getInstance().gameRenderer.mainRenderTarget()
                    //? } else {
                    /*Minecraft.getInstance().getMainRenderTarget()
                     *///? }
            );;
        }

        skyboxify$tickDelta =
                //? >=1.21.9 {
                net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
                 //?} else {
                /*tickDelta;
        *///?}
    }

    @Inject(method = "lambda$addSkyPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderEndSky()V", shift = At.Shift.AFTER))
    private
    //? >=1.21.11
    static
    void skyboxify$renderEndSkybox(
            //? =1.21.10 {
            /*final com.mojang.blaze3d.buffers.GpuBufferSlice skyFog,
            final net.minecraft.client.renderer.state.level.SkyRenderState state,
            *///? }
            final CallbackInfo ci
    ) {
        Skyboxify.getGlobalEventManager().dispatch(new SkyRenderEvent.EndSky.After(skyboxify$skyFeatureRenderer, Minecraft.getInstance().level));
    }

    @WrapOperation(
            method = "lambda$addSkyPass$0",
            at = @At(
                    value = "INVOKE",
                    //? >=1.21.9 {
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;FI)V"
                    //?} else {
                    /*target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FI)V"
                    *///?}
            )
    )
    private
    //? >=1.21.11
    static
    void skyboxify$endBatchSunrise(
            final net.minecraft.client.renderer.SkyRenderer instance,
            final PoseStack poseStack,
            //? >=1.21.4 <1.21.9
            //final net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource,
            final float sunAngle,
            final int sunriseAndSunsetColor,
			final Operation<Void> original
    ) {
        if (!Skyboxify.getGlobalEventManager().dispatch(new SkyRenderEvent.SunriseSunset()).isCancelled()) {
			original.call(
					instance,
					poseStack,
					//? >=1.21.4 <1.21.9
					//bufferSource,
					sunAngle,
					sunriseAndSunsetColor
			);
			Skyboxify.getGlobalEventManager().dispatch(new SkyRenderEvent.SunriseSunset.After(
					//? >=1.21.4 <1.21.9
					//bufferSource
			));
		}
    }

    @WrapWithCondition(
            method = "lambda$addSkyPass$0",
            at = @At(
                    value = "INVOKE",
                    //? >=1.21.11 {
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSkyDisc(I)V"
                    //? } else {
                    /*target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSkyDisc(FFF)V"
                    *///? }
            )
    )
    private
    //? >=1.21.11
    static
    boolean skyboxify$skyDiscEvent$top(
            final net.minecraft.client.renderer.SkyRenderer instance,
            //? >=1.21.11 {
            final int skyColor
            //? } else {
            /*final float red, final float green, final float blue
            *///? }
    ) {
        return !Skyboxify.getGlobalEventManager().dispatch(new SkyRenderEvent.Disc(SkyRenderEvent.Disc.Type.TOP)).isCancelled();
    }

    @WrapWithCondition(
            method = "lambda$addSkyPass$0",
            at = @At(
                    value = "INVOKE",
                    //? >=1.21.11 {
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;FFFLnet/minecraft/world/level/MoonPhase;FF)V"
                    //?} else >=1.21.9 {
                    /*target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;FIFF)V"
                    *///?} else >=1.21.6 {
                    /*target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FIFF)V"
                    *///?} else {
                    /*target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FIFFLnet/minecraft/client/renderer/FogParameters;)V"
                    *///?}
            )
    )
    private
    //? >=1.21.11
    static
    boolean skyboxify$renderSkyboxes(
            final net.minecraft.client.renderer.SkyRenderer instance,
            final PoseStack poseStack,
            //? >=1.21.4 <1.21.9
            //final net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource,
            final float sunAngle,
            //? <=1.21.10 {
            /*final int moonPhase,
            *///?}
            final float moonAngle,
            final float starAngle
            //? >=1.21.11 {
            , final net.minecraft.world.level.MoonPhase moonPhase,
            final float rainBrightness,
            final float starBrightness
            //?}
            //? <1.21.6
            //,final net.minecraft.client.renderer.FogParameters fog
    ) {
        return !Skyboxify.getGlobalEventManager().dispatch(new SkyRenderEvent.SunMoonStars(skyboxify$skyFeatureRenderer, Minecraft.getInstance().level, skyboxify$tickDelta)).isCancelled();
    }

    @WrapWithCondition(
            method = "lambda$addSkyPass$0",
            at = @At(
                    value = "INVOKE",
                    //? >=1.21.8 {
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderDarkDisc()V"
                    //? } else {
                    /*target = "Lnet/minecraft/client/renderer/SkyRenderer;renderDarkDisc(Lcom/mojang/blaze3d/vertex/PoseStack;)V"
                    *///? }
            )
    )
    private
    //? >=1.21.11
    static
    boolean skyboxify$skyDiscEvent$bottom(
            final net.minecraft.client.renderer.SkyRenderer instance
            //? <=1.21.4
            /*, final PoseStack poseStack*/
    ) {
        return !Skyboxify.getGlobalEventManager().dispatch(new SkyRenderEvent.Disc(SkyRenderEvent.Disc.Type.BOTTOM)).isCancelled();
    }

    @WrapOperation(
            method = "addSkyPass",
            at = @At(
                    //? >=1.21.11 {
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/state/level/SkyRenderState;skybox:Lnet/minecraft/world/level/dimension/DimensionType$Skybox;",
                    //?} else >=1.21.10 {
                    /*value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/state/level/SkyRenderState;skyType:Lnet/minecraft/client/renderer/DimensionSpecialEffects$SkyType;",
                    *///?} else {
                    /*value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;skyType()Lnet/minecraft/client/renderer/DimensionSpecialEffects$SkyType;",
                    *///?}
                    opcode = Opcodes.GETFIELD
            )
    )
    private
    //? >=1.21.11 {
    DimensionType.Skybox
     //?} else {
    /*DimensionSpecialEffects.SkyType
    *///?}
    skyboxify$allowNetherSky(
            //? >= 1.21.9 {
            final net.minecraft.client.renderer.state.level.SkyRenderState instance,
             //?} else {
            /*final DimensionSpecialEffects instance,
            *///?}
            final Operation<
                    //? >=1.21.11 {
                    DimensionType.Skybox
                     //?} else {
                    /*DimensionSpecialEffects.SkyType
                    *///?}
            > original
    ) {
        //noinspection DataFlowIssue
        if (SkyboxifyImpl.skyboxManager().isEnabled() && SkyboxifyImpl.skyboxManager().containsEnabled(Level.NETHER) && Minecraft.getInstance().level.dimension().equals(Level.NETHER)) {
            //? >=1.21.11 {
            return DimensionType.Skybox.OVERWORLD;
             //?} else {
            /*return DimensionSpecialEffects.SkyType.OVERWORLD;
            *///?}
        } else {
            return original.call(instance);
        }
    }
}
