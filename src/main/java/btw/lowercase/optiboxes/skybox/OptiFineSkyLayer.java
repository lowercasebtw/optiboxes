package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.OptiBoxesClient;
import btw.lowercase.optiboxes.utils.CommonUtils;
import btw.lowercase.optiboxes.utils.components.*;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class OptiFineSkyLayer implements AutoCloseable {
    public static final Codec<OptiFineSkyLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("source").forGetter(OptiFineSkyLayer::getSource),
            Codec.BOOL.optionalFieldOf("biomeInclusion", true).forGetter(OptiFineSkyLayer::isBiomeInclusion),
            ResourceLocation.CODEC.listOf().optionalFieldOf("biomes", ImmutableList.of()).forGetter(OptiFineSkyLayer::getBiomes),
            Range.CODEC.listOf().optionalFieldOf("heights", ImmutableList.of()).forGetter(OptiFineSkyLayer::getHeights),
            Blend.CODEC.optionalFieldOf("blend", Blend.ADD).forGetter(OptiFineSkyLayer::getBlend),
            Fade.CODEC.optionalFieldOf("fade", Fade.DEFAULT).forGetter(OptiFineSkyLayer::getFade),
            Codec.BOOL.optionalFieldOf("rotate", false).forGetter(OptiFineSkyLayer::isRotate),
            Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(OptiFineSkyLayer::getSpeed),
            ExtraCodecs.VECTOR3F.optionalFieldOf("axis", new Vector3f(1.0F, 0.0F, 0.0F)).forGetter(OptiFineSkyLayer::getAxis),
            Loop.CODEC.optionalFieldOf("loop", Loop.DEFAULT).forGetter(OptiFineSkyLayer::getLoop),
            Codec.FLOAT.optionalFieldOf("transition", 1.0F).forGetter(OptiFineSkyLayer::getTransition),
            Weather.CODEC.listOf().optionalFieldOf("weather", ImmutableList.of(Weather.CLEAR)).forGetter(OptiFineSkyLayer::getWeathers)
    ).apply(instance, OptiFineSkyLayer::new));

    private final ResourceLocation source;
    private final boolean biomeInclusion;
    private final List<ResourceLocation> biomes;
    private final List<Range> heights;
    private final Blend blend;
    private final Fade fade;
    private final boolean rotate;
    private final float speed;
    private final Vector3f axis;
    private final Loop loop;
    private final float transition;
    private final List<Weather> weathers;

    private GpuTexture texture;
    private GpuBuffer skyBuffer;
    private RenderSystem.AutoStorageIndexBuffer skyBufferIndices;
    private int skyBufferIndexCount;
    public float conditionAlpha = -1;

    public OptiFineSkyLayer(ResourceLocation source, boolean biomeInclusion, List<ResourceLocation> biomes, List<Range> heights, Blend blend, Fade fade, boolean rotate, float speed, Vector3f axis, Loop loop, float transition, List<Weather> weathers) {
        this.source = source;
        this.biomeInclusion = biomeInclusion;
        this.biomes = biomes;
        this.heights = heights;
        this.blend = blend;
        this.fade = fade;
        this.rotate = rotate;
        this.speed = speed;
        this.axis = axis;
        this.loop = loop;
        this.transition = transition;
        this.weathers = weathers;
        // Setup sky
        // NOTE: ugh way of doing it but idk how else
        Minecraft.getInstance().execute(() -> {
            this.texture = Minecraft.getInstance().getTextureManager().getTexture(this.source).getTexture();
            try (MeshData meshData = this.buildSky().buildOrThrow()) {
                this.skyBufferIndexCount = meshData.drawState().indexCount();
                this.skyBuffer = RenderSystem.getDevice().createBuffer(() -> "Custom Sky", BufferType.VERTICES, BufferUsage.STATIC_WRITE, meshData.vertexBuffer());
            }

            this.skyBufferIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        });
    }

    private BufferBuilder buildSky() {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        this.renderSide(poseStack, builder, 4);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        this.renderSide(poseStack, builder, 1);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        this.renderSide(poseStack, builder, 0);
        poseStack.popPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        this.renderSide(poseStack, builder, 5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        this.renderSide(poseStack, builder, 2);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        this.renderSide(poseStack, builder, 3);
        poseStack.popPose();
        return builder;
    }

    public void render(Level level, PoseStack poseStack, int timeOfDay, float skyAngle, float rainGradient, float thunderGradient) {
        float weatherAlpha = this.getWeatherAlpha(rainGradient, thunderGradient);
        float fadeAlpha = this.getFadeAlpha(timeOfDay);
        float finalAlpha = Mth.clamp(this.conditionAlpha * weatherAlpha * fadeAlpha, 0.0F, 1.0F);
        if (!(finalAlpha < 1.0E-4F)) {
            poseStack.pushPose();
            if (this.rotate) {
                poseStack.mulPose(Axis.of(this.axis).rotationDegrees(this.getAngle(level, skyAngle)));
            }

            this.blend.apply(finalAlpha);
            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
            matrix4fStack.pushMatrix();
            matrix4fStack.mul(poseStack.last().pose());
            RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
            GpuBuffer indexBuffer = this.skyBufferIndices.getBuffer(this.skyBufferIndexCount);
            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(renderTarget.getColorTexture(), OptionalInt.empty(), renderTarget.getDepthTexture(), OptionalDouble.empty())) {
                renderPass.setPipeline(OptiBoxesClient.getCustomSkyPipeline(this.blend.getBlendFunction()));
                renderPass.setVertexBuffer(0, this.skyBuffer);
                renderPass.setIndexBuffer(indexBuffer, this.skyBufferIndices.type());
                renderPass.bindSampler("Sampler0", this.texture);
                renderPass.drawIndexed(0, this.skyBufferIndexCount);
            }
            matrix4fStack.popMatrix();
            poseStack.popPose();
        }
    }

    private void renderSide(PoseStack poseStack, VertexConsumer vertexConsumer, int side) {
        float u = (float) (side % 3) / 3.0F;
        float v = (float) (side / 3) / 2.0F;
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(u, v);
        vertexConsumer.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(u, v + 0.5F);
        vertexConsumer.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(u + 0.33333334F, v + 0.5F);
        vertexConsumer.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(u + 0.33333334F, v);
    }

    private float getAngle(Level level, float skyAngle) {
        float angleDayStart = 0.0F;
        if (this.speed != (float) Math.round(this.speed)) {
            long currentWorldDay = (level.getDayTime() + 18000L) / 24000L;
            double anglePerDay = this.speed % 1.0F;
            double currentAngle = (double) currentWorldDay * anglePerDay;
            angleDayStart = (float) (currentAngle % 1.0D);
        }

        return 360.0F * (angleDayStart + skyAngle * this.speed);
    }

    private boolean getConditionCheck(Level level) {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return false;
        }

        BlockPos entityPos = cameraEntity.getOnPos();
        if (!this.biomes.isEmpty()) {
            Holder<Biome> currentBiome = level.getBiome(entityPos);
            if (!currentBiome.isBound()) {
                return false;
            }

            if (!(this.biomeInclusion && this.biomes.contains(level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(currentBiome.value())))) {
                return false;
            }
        }

        return this.heights == null || CommonUtils.checkRanges(entityPos.getY(), this.heights);
    }

    public void tick(Level level) {
        this.conditionAlpha = this.getPositionBrightness(level);
    }

    private float getPositionBrightness(Level level) {
        if (this.biomes.isEmpty() && this.heights.isEmpty()) {
            return 1.0F;
        }

        if (this.conditionAlpha == -1) {
            boolean conditionCheck = this.getConditionCheck(level);
            return conditionCheck ? 1.0F : 0.0F;
        }

        return CommonUtils.calculateConditionAlphaValue(1.0F, 0.0F, this.conditionAlpha, (int) (this.transition * 20), this.getConditionCheck(level));
    }

    private float getWeatherAlpha(float rainStrength, float thunderStrength) {
        float f = 1.0F - rainStrength;
        float f1 = rainStrength - thunderStrength;
        float weatherAlpha = 0.0F;
        if (this.weathers.contains(Weather.CLEAR)) {
            weatherAlpha += f;
        }

        if (this.weathers.contains(Weather.RAIN)) {
            weatherAlpha += f1;
        }

        if (this.weathers.contains(Weather.THUNDER)) {
            weatherAlpha += thunderStrength;
        }

        return Mth.clamp(weatherAlpha, 0.0F, 1.0F);
    }

    private float getFadeAlpha(int timeOfDay) {
        if (!this.fade.alwaysOn()) {
            return CommonUtils.calculateFadeAlphaValue(1.0F, 0.0F, timeOfDay, this.fade.startFadeIn(), this.fade.endFadeIn(), this.fade.startFadeOut(), this.fade.endFadeOut());
        } else {
            return 1.0F;
        }
    }

    public boolean isActive(long dayTime, int clampedTimeOfDay) {
        if (!this.fade.alwaysOn() && CommonUtils.isInTimeInterval(clampedTimeOfDay, this.fade.endFadeOut(), this.fade.startFadeIn())) {
            return false;
        } else if (this.loop.ranges() != null) {
            long adjustedTime = dayTime - (long) this.fade.startFadeIn();
            while (adjustedTime < 0L) {
                adjustedTime += 24000L * (int) this.loop.days();
            }

            int daysPassed = (int) (adjustedTime / 24000L);
            int currentDay = daysPassed % (int) this.loop.days();
            return CommonUtils.checkRanges(currentDay, this.loop.ranges());
        } else {
            return true;
        }
    }

    public ResourceLocation getSource() {
        return source;
    }

    public boolean isBiomeInclusion() {
        return biomeInclusion;
    }

    public List<ResourceLocation> getBiomes() {
        return biomes;
    }

    public List<Range> getHeights() {
        return heights;
    }

    public Blend getBlend() {
        return blend;
    }

    public Fade getFade() {
        return fade;
    }

    public boolean isRotate() {
        return rotate;
    }

    public float getSpeed() {
        return speed;
    }

    public Vector3f getAxis() {
        return axis;
    }

    public Loop getLoop() {
        return loop;
    }

    public float getTransition() {
        return transition;
    }

    public List<Weather> getWeathers() {
        return weathers;
    }

    public void setConditionAlpha(float conditionAlpha) {
        this.conditionAlpha = conditionAlpha;
    }

    @Override
    public void close() {
        this.texture.close();
        this.skyBuffer.close();
        this.skyBufferIndexCount = -1;
    }
}
