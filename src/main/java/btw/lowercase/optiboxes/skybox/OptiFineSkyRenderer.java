package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.OptiBoxesClient;
import btw.lowercase.optiboxes.utils.CommonUtils;
import btw.lowercase.optiboxes.utils.DynamicTransformsBuilder;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class OptiFineSkyRenderer {
    private GpuBuffer skyBuffer;
    private RenderSystem.AutoStorageIndexBuffer skyBufferIndices;
    private int skyBufferIndexCount;
    private final Map<ResourceLocation, GpuTexture> textureCache = new HashMap<>();
    private final Map<ResourceLocation, RenderPipeline> renderPipelineCache = new HashMap<>();

    private BufferBuilder buildSky() {
        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION_TEX.getVertexSize() * 24);
        BufferBuilder builder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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

    private void renderSide(PoseStack poseStack, VertexConsumer vertexConsumer, int side) {
        float u = (float) (side % 3) / 3.0F;
        float v = (float) (side / 3) / 2.0F;
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(u, v);
        vertexConsumer.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(u, v + 0.5F);
        vertexConsumer.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(u + 0.33333334F, v + 0.5F);
        vertexConsumer.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(u + 0.33333334F, v);
    }

    private float getAngle(Level level, float skyAngle, float speed) {
        float angleDayStart = 0.0F;
        if (speed != (float) Math.round(speed)) {
            long currentWorldDay = (level.getDayTime() + 18000L) / 24000L;
            double anglePerDay = speed % 1.0F;
            double currentAngle = (double) currentWorldDay * anglePerDay;
            angleDayStart = (float) (currentAngle % 1.0D);
        }

        return -360.0F * (angleDayStart + skyAngle * speed);
    }

    public void renderSkybox(OptiFineSkybox optiFineSkybox, PoseStack poseStack, Level level, float tickDelta) {
        if (this.skyBuffer == null) {
            try (MeshData meshData = this.buildSky().buildOrThrow()) {
                this.skyBufferIndexCount = meshData.drawState().indexCount();
                this.skyBuffer = RenderSystem.getDevice().createBuffer(() -> "Custom Sky", GpuBuffer.USAGE_COPY_DST, meshData.vertexBuffer());
            }
        }

        if (this.skyBufferIndices == null) {
            this.skyBufferIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        }

        long dayTime = level.getDayTime();
        int clampedTimeOfDay = (int) (dayTime % 24000L);
        float skyAngle = level.getTimeOfDay(tickDelta);
        float thunderLevel = level.getThunderLevel(tickDelta);
        float rainLevel = level.getRainLevel(tickDelta);
        if (rainLevel > 0.0F) {
            thunderLevel /= rainLevel;
        }

        for (OptiFineSkyLayer optiFineSkyLayer : optiFineSkybox.getLayers().stream().filter(layer -> layer.isActive(dayTime, clampedTimeOfDay)).toList()) {
            renderSkyLayer(optiFineSkyLayer, level, poseStack, clampedTimeOfDay, skyAngle, rainLevel, thunderLevel, optiFineSkybox.getConditionAlphaFor(optiFineSkyLayer));
        }
    }

    public void renderSkyLayer(OptiFineSkyLayer optiFineSkyLayer, Level level, PoseStack poseStack, int timeOfDay, float skyAngle, float rainGradient, float thunderGradient, float conditionAlpha) {
        float weatherAlpha = CommonUtils.getWeatherAlpha(optiFineSkyLayer.weatherConditions(), rainGradient, thunderGradient);
        float fadeAlpha = optiFineSkyLayer.fade().getAlpha(timeOfDay);
        float finalAlpha = Mth.clamp(conditionAlpha * weatherAlpha * fadeAlpha, 0.0F, 1.0F);
        if (!(finalAlpha < 1.0E-4F)) {
            poseStack.pushPose();
            if (optiFineSkyLayer.rotate()) {
                poseStack.mulPose(Axis.of(optiFineSkyLayer.axis()).rotationDegrees(this.getAngle(level, skyAngle, optiFineSkyLayer.speed())));
            }

            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
            matrix4fStack.pushMatrix();
            matrix4fStack.mul(poseStack.last().pose());

            GpuBufferSlice transforms = DynamicTransformsBuilder.of()
                    .withShaderColor(optiFineSkyLayer.blend().getShaderColor(finalAlpha))
                    .build();

            RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
            GpuBuffer indexBuffer = this.skyBufferIndices.getBuffer(this.skyBufferIndexCount);
            GpuTexture texture = this.textureCache.computeIfAbsent(optiFineSkyLayer.source(), (resourceLocation) -> Minecraft.getInstance().getTextureManager().getTexture(resourceLocation).getTexture());
            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "Custom Sky Rendering", renderTarget.getColorTexture(), OptionalInt.empty(), renderTarget.getDepthTexture(), OptionalDouble.empty())) {
                RenderPipeline renderPipeline = this.renderPipelineCache.computeIfAbsent(optiFineSkyLayer.source(), (resourceLocation) -> OptiBoxesClient.getCustomSkyPipeline(optiFineSkyLayer.blend().getBlendFunction()));
                renderPass.setPipeline(renderPipeline);
                renderPass.setVertexBuffer(0, this.skyBuffer);
                renderPass.setIndexBuffer(indexBuffer, this.skyBufferIndices.type());
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", transforms);
                renderPass.bindSampler("Sampler0", texture);
                renderPass.drawIndexed(0, 0, this.skyBufferIndexCount, 1);
            }

            matrix4fStack.popMatrix();
            poseStack.popPose();
        }
    }

    public void clearCache() {
        for (GpuTexture texture : this.textureCache.values()) {
            texture.close();
        }

        this.textureCache.clear();
        this.renderPipelineCache.clear();
    }
}
