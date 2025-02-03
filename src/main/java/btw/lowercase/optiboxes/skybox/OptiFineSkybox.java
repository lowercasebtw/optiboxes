package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.utils.components.Blend;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);

        int skyColor = level.getSkyColor(cameraPosition, tickDelta);
        skyRenderer.renderSkyDisc(ARGB.redFloat(skyColor), ARGB.greenFloat(skyColor), ARGB.blueFloat(skyColor));

        // Sunrise/Sunset
        float timeOfDay = level.getTimeOfDay(tickDelta);
        DimensionSpecialEffects effects = level.effects();
        if (effects.isSunriseOrSunset(timeOfDay)) {
            float sunAngle = level.getSunAngle(tickDelta);
            int sunriseOrSunsetColor = effects.getSunriseOrSunsetColor(timeOfDay);
            skyRenderer.renderSunriseAndSunset(poseStack, bufferSource, sunAngle, sunriseOrSunsetColor);
            bufferSource.endBatch();
        }

        // OptiFine Sky Rendering
        {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            poseStack.pushPose();
            float rainLevel = 1.0F - level.getRainLevel(tickDelta);
            float starBrightness = level.getStarBrightness(tickDelta) * rainLevel;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainLevel); // TODO/NOTE: needed?
            this.render(poseStack, level, tickDelta);
            poseStack.popPose();
            skyRenderer.renderSunMoonAndStars(poseStack, bufferSource, timeOfDay, level.getMoonPhase(), rainLevel, starBrightness, fogParameters);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

        // Dark Disc
        if (this.shouldRenderDarkDisc(level, tickDelta)) {
            skyRenderer.renderDarkDisc(poseStack);
        }

        RenderSystem.depthMask(true);
    }

    private boolean shouldRenderDarkDisc(ClientLevel level, float tickDelta) {
        Minecraft minecraft = Minecraft.getInstance();
        assert minecraft.player != null;
        return minecraft.player.getEyePosition(tickDelta).y - level.getLevelData().getHorizonHeight(level) < 0.0;
    }

    private void render(PoseStack poseStack, Level level, float tickDelta) {
        long timeOfDay = level.getDayTime();
        int clampedTimeOfDay = (int) (timeOfDay % 24000L);
        float skyAngle = level.getTimeOfDay(tickDelta);
        float thunderLevel = level.getThunderLevel(tickDelta);
        float rainLevel = level.getRainLevel(tickDelta);
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
        if (level.dimension().equals(this.worldResourceKey)) {
            this.layers.forEach(layer -> layer.tick(level));
        } else {
            this.layers.forEach(layer -> layer.setConditionAlpha(-1.0F));
            this.active = false;
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
