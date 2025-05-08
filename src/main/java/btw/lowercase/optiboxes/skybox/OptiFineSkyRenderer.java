package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.OptiBoxesClient;
import btw.lowercase.optiboxes.mixins.RenderPipelinesAccessor;
import btw.lowercase.optiboxes.utils.CommonUtils;
import btw.lowercase.optiboxes.utils.DynamicTransformsBuilder;
import btw.lowercase.optiboxes.utils.UVRange;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.*;

import java.lang.Math;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class OptiFineSkyRenderer {
    public static final OptiFineSkyRenderer INSTANCE = new OptiFineSkyRenderer();

    private GpuBuffer skyBuffer;
    private RenderSystem.AutoStorageIndexBuffer skyBufferIndices;
    private int skyBufferIndexCount;
    private final Map<ResourceLocation, GpuTextureView> textureCache = new HashMap<>();
    private final Map<ResourceLocation, RenderPipeline> renderPipelineCache = new HashMap<>();

    private OptiFineSkyRenderer() {
        Minecraft.getInstance().schedule(this::buildSky);
    }

    private void buildSky() {
        VertexFormat vertexFormat = DefaultVertexFormat.POSITION_TEX;
        VertexFormat.Mode vertexFormatMode = VertexFormat.Mode.QUADS;

        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(vertexFormat.getVertexSize() * 24);
        BufferBuilder builder = new BufferBuilder(byteBufferBuilder, vertexFormatMode, vertexFormat);
        for (int face = 0; face < 6; ++face) {
            UVRange uvRange = CommonUtils.getUvRangeForFace(face);
            Matrix4f matrix4f = CommonUtils.getRotationMatrixForFace(face);
            final float quadSize = 100.0F;
            builder.addVertex(CommonUtils.getMatrixTransform(matrix4f, -quadSize, -quadSize, -quadSize)).setUv(uvRange.minU(), uvRange.minV());
            builder.addVertex(CommonUtils.getMatrixTransform(matrix4f, -quadSize, -quadSize, quadSize)).setUv(uvRange.minU(), uvRange.maxV());
            builder.addVertex(CommonUtils.getMatrixTransform(matrix4f, quadSize, -quadSize, quadSize)).setUv(uvRange.maxU(), uvRange.maxV());
            builder.addVertex(CommonUtils.getMatrixTransform(matrix4f, quadSize, -quadSize, -quadSize)).setUv(uvRange.maxU(), uvRange.minV());
        }

        skyBufferIndices = RenderSystem.getSequentialBuffer(vertexFormatMode);
        try (MeshData meshData = builder.build()) {
            if (meshData != null) {
                skyBufferIndexCount = meshData.drawState().indexCount();
                skyBuffer = RenderSystem.getDevice().createBuffer(() -> "OptiFine skybox", GpuBuffer.USAGE_COPY_DST, meshData.vertexBuffer());
            }
        }
    }

    public static RenderPipeline getCustomSkyPipeline(BlendFunction blendFunction) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelinesAccessor.getMatricesProjectionSnippet());
        builder.withLocation(OptiBoxesClient.id("pipeline/custom_skybox"));
        builder.withVertexShader(OptiBoxesClient.id("core/custom_skybox"));
        builder.withFragmentShader(OptiBoxesClient.id("core/custom_skybox"));
        builder.withDepthWrite(false);
        builder.withColorWrite(true, false);
        if (blendFunction != null) {
            builder.withBlend(blendFunction);
        }
        builder.withSampler("Sampler0");
        builder.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);
        return builder.build();
    }

    public void renderSkybox(OptiFineSkybox optiFineSkybox, Matrix4fStack modelViewStack, Level level, float tickDelta) {
        long dayTime = level.getDayTime();
        int clampedTimeOfDay = (int) (dayTime % 24000L);
        float skyAngle = level.getTimeOfDay(tickDelta);
        float thunderLevel = level.getThunderLevel(tickDelta);
        float rainLevel = level.getRainLevel(tickDelta);
        if (rainLevel > 0.0F) {
            thunderLevel /= rainLevel;
        }

        for (OptiFineSkyLayer optiFineSkyLayer : optiFineSkybox.getLayers().stream().filter(layer -> layer.isActive(dayTime, clampedTimeOfDay)).toList()) {
            renderSkyLayer(optiFineSkyLayer, modelViewStack, level, clampedTimeOfDay, skyAngle, rainLevel, thunderLevel, optiFineSkybox.getConditionAlphaFor(optiFineSkyLayer));
        }
    }

    public void renderSkyLayer(OptiFineSkyLayer optiFineSkyLayer, Matrix4fStack modelViewStack, Level level, int timeOfDay, float skyAngle, float rainGradient, float thunderGradient, float conditionAlpha) {
        float weatherAlpha = CommonUtils.getWeatherAlpha(optiFineSkyLayer.weatherConditions(), rainGradient, thunderGradient);
        float fadeAlpha = optiFineSkyLayer.fade().getAlpha(timeOfDay);
        float finalAlpha = Mth.clamp(conditionAlpha * weatherAlpha * fadeAlpha, 0.0F, 1.0F);
        if (!(finalAlpha < 1.0E-4F) && this.skyBuffer != null) {
            modelViewStack.pushMatrix();
            if (optiFineSkyLayer.rotate()) {
                // NOTE: Using `mulPose` directly gives a different result.
                final float angle = this.getAngle(level, skyAngle, optiFineSkyLayer.speed());
                modelViewStack.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(angle), optiFineSkyLayer.axis())));
            }

            GpuBufferSlice transforms = DynamicTransformsBuilder.of()
                    .withModelViewMatrix(modelViewStack)
                    .withShaderColor(optiFineSkyLayer.blend().getShaderColor(finalAlpha))
                    .build();

            RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
            GpuBuffer indexBuffer = this.skyBufferIndices.getBuffer(this.skyBufferIndexCount);
            GpuTextureView texture = this.textureCache.computeIfAbsent(optiFineSkyLayer.source(), (resourceLocation) -> Minecraft.getInstance().getTextureManager().getTexture(resourceLocation).getTextureView());
            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "Custom Sky Rendering", renderTarget.getColorTextureView(), OptionalInt.empty(), renderTarget.getDepthTextureView(), OptionalDouble.empty())) {
                RenderPipeline renderPipeline = this.renderPipelineCache.computeIfAbsent(optiFineSkyLayer.source(), (resourceLocation) -> getCustomSkyPipeline(optiFineSkyLayer.blend().getBlendFunction()));
                renderPass.setPipeline(renderPipeline);
                renderPass.setVertexBuffer(0, this.skyBuffer);
                renderPass.setIndexBuffer(indexBuffer, this.skyBufferIndices.type());
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", transforms);
                renderPass.bindSampler("Sampler0", texture);
                renderPass.drawIndexed(0, 0, this.skyBufferIndexCount, 1);
            }

            modelViewStack.popMatrix();
        }
    }

    private float getAngle(Level level, float skyAngle, float speed) {
        float angleDayStart = 0.0F;
        if (speed != (float) Math.round(speed)) {
            long currentWorldDay = (level.getDayTime() + 18000L) / 24000L;
            double anglePerDay = speed % 1.0F;
            double currentAngle = (double) currentWorldDay * anglePerDay;
            angleDayStart = (float) (currentAngle % 1.0D);
        }

        return 360.0F * (angleDayStart + skyAngle * speed);
    }

    public void clearCache() {
        for (GpuTextureView textureView : this.textureCache.values()) {
            textureView.texture().close();
            textureView.close();
        }

        this.textureCache.clear();
        this.renderPipelineCache.clear();
    }
}
