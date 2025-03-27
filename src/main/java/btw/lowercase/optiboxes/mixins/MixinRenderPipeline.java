package btw.lowercase.optiboxes.mixins;

import btw.lowercase.optiboxes.CopyableRenderPipeline;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Optional;

@Mixin(RenderPipeline.class)
public abstract class MixinRenderPipeline implements CopyableRenderPipeline {
    @Shadow
    public abstract ResourceLocation getLocation();

    @Shadow
    public abstract ResourceLocation getFragmentShader();

    @Shadow
    public abstract ResourceLocation getVertexShader();

    @Shadow
    public abstract ShaderDefines getShaderDefines();

    @Shadow
    public abstract List<String> getSamplers();

    @Shadow
    public abstract List<RenderPipeline.UniformDescription> getUniforms();

    @Shadow
    public abstract DepthTestFunction getDepthTestFunction();

    @Shadow
    public abstract PolygonMode getPolygonMode();

    @Shadow
    public abstract boolean isCull();

    @Shadow
    public abstract boolean isWriteColor();

    @Shadow
    public abstract boolean isWriteAlpha();

    @Shadow
    public abstract boolean isWriteDepth();

    @Shadow
    public abstract LogicOp getColorLogic();

    @Shadow
    public abstract Optional<BlendFunction> getBlendFunction();

    @Shadow
    public abstract VertexFormat getVertexFormat();

    @Shadow
    public abstract VertexFormat.Mode getVertexFormatMode();

    @Shadow
    public abstract float getDepthBiasScaleFactor();

    @Shadow
    public abstract float getDepthBiasConstant();

    @Unique
    public RenderPipeline.Builder optiboxes$copy() {
        RenderPipeline.Builder builder = RenderPipeline.builder();
        builder.withLocation(this.getLocation());
        builder.withFragmentShader(this.getFragmentShader());
        builder.withVertexShader(this.getVertexShader());
        this.getShaderDefines().flags().forEach(builder::withShaderDefine);
        this.getSamplers().forEach(builder::withSampler);
        for (RenderPipeline.UniformDescription uniformDescription : this.getUniforms()) {
            builder.withUniform(uniformDescription.name(), uniformDescription.type());
        }

        builder.withDepthTestFunction(this.getDepthTestFunction());
        builder.withPolygonMode(this.getPolygonMode());
        builder.withCull(this.isCull());
        builder.withColorWrite(this.isWriteColor(), this.isWriteAlpha());
        builder.withDepthWrite(this.isWriteDepth());
        builder.withColorLogic(this.getColorLogic());
        if (this.getBlendFunction().isPresent()) {
            builder.withBlend(this.getBlendFunction().get());
        } else {
            builder.withoutBlend();
        }

        builder.withVertexFormat(this.getVertexFormat(), this.getVertexFormatMode());
        builder.withDepthBias(this.getDepthBiasScaleFactor(), this.getDepthBiasConstant());
        return builder;
    }
}
