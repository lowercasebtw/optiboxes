package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.utils.CommonUtils;
import btw.lowercase.optiboxes.utils.components.Blend;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class OptiFineSkyRenderer {
    private VertexBuffer skyBuffer = null;

    private void buildSky(VertexConsumer vertexConsumer) {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        this.renderSide(poseStack, vertexConsumer, 4);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        this.renderSide(poseStack, vertexConsumer, 1);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        this.renderSide(poseStack, vertexConsumer, 0);
        poseStack.popPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        this.renderSide(poseStack, vertexConsumer, 5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        this.renderSide(poseStack, vertexConsumer, 2);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        this.renderSide(poseStack, vertexConsumer, 3);
        poseStack.popPose();
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

        return 360.0F * (angleDayStart + skyAngle * speed);
    }

    public void renderSkybox(OptiFineSkybox optiFineSkybox, PoseStack poseStack, Level level, float tickDelta) {
        if (this.skyBuffer == null) {
            this.skyBuffer = VertexBuffer.uploadStatic(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, this::buildSky);
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
            renderSkyLayer(optiFineSkyLayer, level, poseStack, clampedTimeOfDay, skyAngle, rainLevel, thunderLevel);
        }

        Blend.ADD.apply(1.0F - rainLevel);
    }

    public void renderSkyLayer(OptiFineSkyLayer optiFineSkyLayer, Level level, PoseStack poseStack, int timeOfDay, float skyAngle, float rainGradient, float thunderGradient) {
        float weatherAlpha = CommonUtils.getWeatherAlpha(optiFineSkyLayer.getWeatherConditions(), rainGradient, thunderGradient);
        float fadeAlpha = CommonUtils.getFadeAlpha(optiFineSkyLayer.getFade(), timeOfDay);
        float finalAlpha = Mth.clamp(optiFineSkyLayer.conditionAlpha * weatherAlpha * fadeAlpha, 0.0F, 1.0F);
        if (!(finalAlpha < 1.0E-4F) && this.skyBuffer != null) {
            poseStack.pushPose();
            if (optiFineSkyLayer.shouldRotate()) {
                poseStack.mulPose(Axis.of(optiFineSkyLayer.getAxis()).rotationDegrees(this.getAngle(level, skyAngle, optiFineSkyLayer.getSpeed())));
            }

            RenderSystem.setShaderTexture(0, optiFineSkyLayer.getSource());
            try (CompiledShaderProgram compiledShaderProgram = RenderSystem.setShader(CoreShaders.POSITION_TEX)) {
                Blend blend = optiFineSkyLayer.getBlend();
                blend.apply(finalAlpha);
                if (blend.getBlendFunction() != null) {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(blend.getBlendFunction().sourceFactor(), blend.getBlendFunction().destFactor());
                } else {
                    RenderSystem.disableBlend();
                }

                Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
                matrix4fStack.pushMatrix();
                matrix4fStack.mul(poseStack.last().pose());
                this.skyBuffer.bind();
                this.skyBuffer.drawWithShader(matrix4fStack, RenderSystem.getProjectionMatrix(), compiledShaderProgram);
                VertexBuffer.unbind();
                matrix4fStack.popMatrix();
            }

            poseStack.popPose();
        }
    }
}
