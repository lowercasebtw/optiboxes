package btw.lowercase.optiboxes.utils.components;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Blend {
    ALPHA(alpha -> RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha), new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)),
    ADD(alpha -> RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha), new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)),
    SUBTRACT(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO)),
    MULTIPLY(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, alpha), new BlendFunction(SourceFactor.DST_COLOR, DestFactor.ONE_MINUS_SRC_ALPHA)),
    DODGE(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.ONE, DestFactor.ONE)),
    BURN(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.ZERO, DestFactor.ONE_MINUS_SRC_COLOR)),
    SCREEN(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_COLOR)),
    OVERLAY(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR)),
    REPLACE(alpha -> RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha), null);

    public static final Codec<Blend> CODEC = Codec.STRING.xmap(Blend::byName, Blend::toString);

    private final Consumer<Float> blendConsumer;
    private final BlendFunction blendFunction;

    Blend(Consumer<Float> blendConsumer, BlendFunction blendFunction) {
        this.blendConsumer = blendConsumer;
        this.blendFunction = blendFunction;
    }

    public static Blend byName(String name) {
        return Arrays.stream(Blend.values()).filter(blend -> blend.toString().toLowerCase().equals(name)).findFirst().orElse(ADD);
    }

    public void apply(float value) {
        this.blendConsumer.accept(value);
    }

    public BlendFunction getBlendFunction() {
        return this.blendFunction;
    }
}
