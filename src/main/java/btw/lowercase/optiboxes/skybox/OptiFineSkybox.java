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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class OptiFineSkybox {
    public static final Codec<OptiFineSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OptiFineSkyLayer.CODEC.listOf().optionalFieldOf("layers", ImmutableList.of()).forGetter(OptiFineSkybox::getLayers),
            Level.RESOURCE_KEY_CODEC.fieldOf("world").forGetter(OptiFineSkybox::getWorldResourceKey)
    ).apply(instance, OptiFineSkybox::new));

    private final List<OptiFineSkyLayer> layers;
    private final ResourceKey<Level> worldResourceKey;
    private boolean active = true;

    public OptiFineSkybox(List<OptiFineSkyLayer> layers, ResourceKey<Level> worldResourceKey) {
        this.layers = layers;
        this.worldResourceKey = worldResourceKey;
    }

    public void renderEndSky(SkyRenderer skyRenderer, PoseStack poseStack, ClientLevel level) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        skyRenderer.renderEndSky();
        this.render(poseStack, level, 0.0F);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public void renderOverworldSky(SkyRenderer skyRenderer, PoseStack poseStack, float tickDelta, ClientLevel level, Vec3 cameraPosition, MultiBufferSource.BufferSource bufferSource, FogParameters fogParameters) {
        int skyColor = level.getSkyColor(cameraPosition, tickDelta);
        float skyRed = ARGB.redFloat(skyColor);
        float skyGreen = ARGB.greenFloat(skyColor);
        float skyBlue = ARGB.blueFloat(skyColor);
        float sunAngle = level.getSunAngle(tickDelta);
        float timeOfDay = level.getTimeOfDay(tickDelta);
        float rainLevel = 1.0F - level.getRainLevel(tickDelta);
        float starBrightness = level.getStarBrightness(tickDelta) * rainLevel;

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        skyRenderer.renderSkyDisc(skyRed, skyGreen, skyBlue);

        // Sunrise/Sunset
        DimensionSpecialEffects effects = level.effects();
        if (effects.isSunriseOrSunset(timeOfDay)) {
            int sunriseOrSunsetColor = effects.getSunriseOrSunsetColor(timeOfDay);
            if (OptiBoxesConfig.instance().useNewSunriseRendering || level.isRaining() || level.isThundering()) {
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
            this.render(poseStack, level, tickDelta);
            poseStack.popPose();
            if (OptiBoxesConfig.instance().renderSunMoonStars) {
                skyRenderer.renderSunMoonAndStars(poseStack, bufferSource, timeOfDay, level.getMoonPhase(), rainLevel, starBrightness, fogParameters);
            }
            bufferSource.endBatch();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

        // Dark Disc
        if (this.shouldRenderDarkDisc(level, tickDelta)) {
            skyRenderer.renderDarkDisc(poseStack);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    private boolean shouldRenderDarkDisc(ClientLevel level, float tickDelta) {
        Minecraft minecraft = Minecraft.getInstance();
        assert minecraft.player != null;
        return minecraft.player.getEyePosition(tickDelta).y - level.getLevelData().getHorizonHeight(level) < 0.0;
    }

    private void render(PoseStack poseStack, Level level, float tickDelta) {
        int timeOfDay = (int) level.getDayTime();
        int clampedTimeOfDay = (int) (timeOfDay % 24000L);
        float skyAngle = level.getTimeOfDay(tickDelta);
        float rainLevel = level.getRainLevel(tickDelta);
        float thunderLevel = level.getThunderLevel(tickDelta);
        if (rainLevel > 0.0F) {
            thunderLevel /= rainLevel;
        }

        for (OptiFineSkyLayer optiFineSkyLayer : this.layers) {
            if (optiFineSkyLayer.isActive(timeOfDay, clampedTimeOfDay)) {
                optiFineSkyLayer.render(level, poseStack, clampedTimeOfDay, skyAngle, rainLevel, thunderLevel);
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
