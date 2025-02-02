package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.config.OptiBoxesConfig;
import btw.lowercase.optiboxes.utils.components.Blend;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import java.util.List;

public class OptiFineSkybox {
    public static final Codec<OptiFineSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OptiFineSkyLayer.CODEC.listOf().optionalFieldOf("layers", ImmutableList.of()).forGetter(OptiFineSkybox::getLayers),
            Level.RESOURCE_KEY_CODEC.fieldOf("world").forGetter(OptiFineSkybox::getWorldResourceKey)
    ).apply(instance, OptiFineSkybox::new));

    private final List<OptiFineSkyLayer> layers;
    private final ResourceKey<Level> worldResourceKey;
    private ClientLevel level = Minecraft.getInstance().level;
    private boolean active = true;

    public OptiFineSkybox(List<OptiFineSkyLayer> layers, ResourceKey<Level> worldResourceKey) {
        this.layers = layers;
        this.worldResourceKey = worldResourceKey;
    }

    public void render(SkyRenderer skyRenderer, PoseStack poseStack, float tickDelta, Camera camera, MultiBufferSource.BufferSource bufferSource, FogParameters fogParameters) {
        this.level = (ClientLevel) camera.getEntity().getCommandSenderWorld();
        this.renderSky(skyRenderer, poseStack, tickDelta, camera, fogParameters, bufferSource);
    }

    // TODO: Move this to MixinLevelRenderer for other sky modifying support
    public void renderSky(SkyRenderer skyRenderer, PoseStack poseStack, float tickDelta, Camera camera, FogParameters fogParameters, MultiBufferSource.BufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderFog(fogParameters);

        DimensionSpecialEffects effects = this.level.effects();
        if (effects.skyType() == DimensionSpecialEffects.SkyType.END) {
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            skyRenderer.renderEndSky();
            this.render(poseStack, 0.0F);
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            return;
        } else if (effects.skyType() != DimensionSpecialEffects.SkyType.OVERWORLD) {
            return;
        }

        int skyColor = this.level.getSkyColor(camera.getPosition(), tickDelta);
        float skyRed = ARGB.redFloat(skyColor);
        float skyGreen = ARGB.greenFloat(skyColor);
        float skyBlue = ARGB.blueFloat(skyColor);
        float sunAngle = this.level.getSunAngle(tickDelta);
        float timeOfDay = this.level.getTimeOfDay(tickDelta);
        float rainLevel = 1.0F - this.level.getRainLevel(tickDelta);
        float starBrightness = this.level.getStarBrightness(tickDelta) * rainLevel;

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        skyRenderer.renderSkyDisc(skyRed, skyGreen, skyBlue);

        // Sunrise/Sunset
        if (effects.isSunriseOrSunset(timeOfDay)) {
            int sunriseOrSunsetColor = effects.getSunriseOrSunsetColor(timeOfDay);
            if (OptiBoxesConfig.instance().useNewSunriseRendering || this.level.isRaining() || this.level.isThundering()) {
                // TODO/NOTE: Fix for broken sky when raining/thundering?
                skyRenderer.renderSunriseAndSunset(poseStack, bufferSource, sunAngle, sunriseOrSunsetColor);
            } else {
                RenderSystem.setShader(CoreShaders.POSITION_COLOR);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(sunAngle) < 0.0F ? 180.0F : 0.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                Matrix4f matrix4f = poseStack.last().pose();
                BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                builder.addVertex(matrix4f, 0.0F, 100.0F, 0.0F).setColor(sunriseOrSunsetColor);
                for (int n = 0; n <= 16; ++n) {
                    float o = (float) n * (float) (Math.PI * 2) / 16.0F;
                    float q = Mth.cos(o);
                    builder.addVertex(matrix4f, Mth.sin(o) * 120.0F, q * 120.0F, -q * 40.0F * ARGB.alphaFloat(sunriseOrSunsetColor)).setColor(ARGB.transparent(sunriseOrSunsetColor));
                }
                BufferUploader.drawWithShader(builder.buildOrThrow());
                poseStack.popPose();
            }
        }

        // OptiFine Sky Rendering
        {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            poseStack.pushPose();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainLevel); // TODO/NOTE: needed?
            this.render(poseStack, tickDelta);
            poseStack.popPose();
            if (OptiBoxesConfig.instance().renderSunMoonStars) {
                skyRenderer.renderSunMoonAndStars(poseStack, bufferSource, timeOfDay, this.level.getMoonPhase(), rainLevel, starBrightness, fogParameters);
            }
            bufferSource.endBatch();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

        // Dark Disc
        if (this.shouldRenderDarkDisc(minecraft, tickDelta)) {
            skyRenderer.renderDarkDisc(poseStack);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    private boolean shouldRenderDarkDisc(Minecraft minecraft, float tickDelta) {
        assert minecraft.player != null;
        return minecraft.player.getEyePosition(tickDelta).y - this.level.getLevelData().getHorizonHeight(this.level) < 0.0;
    }

    private void render(PoseStack poseStack, float tickDelta) {
        int timeOfDay = (int) this.level.getDayTime();
        int clampedTimeOfDay = (int) (timeOfDay % 24000L);
        float skyAngle = this.level.getTimeOfDay(tickDelta);
        float rainLevel = this.level.getRainLevel(tickDelta);
        float thunderLevel = this.level.getThunderLevel(tickDelta);
        if (rainLevel > 0.0F) {
            thunderLevel /= rainLevel;
        }

        for (OptiFineSkyLayer optiFineSkyLayer : this.layers) {
            if (optiFineSkyLayer.isActive(timeOfDay, clampedTimeOfDay)) {
                optiFineSkyLayer.render(this.level, poseStack, clampedTimeOfDay, skyAngle, rainLevel, thunderLevel);
            }
        }

        Blend.ADD.apply(1.0F - rainLevel);
    }

    public void tick(ClientLevel level) {
        this.active = true;
        if (!level.dimension().equals(this.worldResourceKey)) {
            this.layers.forEach(layer -> layer.setConditionAlpha(-1.0F));
            this.active = false;
        } else {
            this.layers.forEach(layer -> layer.tick(level));
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public List<OptiFineSkyLayer> getLayers() {
        return layers;
    }

    public ResourceKey<Level> getWorldResourceKey() {
        return worldResourceKey;
    }
}
