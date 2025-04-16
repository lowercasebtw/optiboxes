package btw.lowercase.optiboxes.utils.components;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.serialization.Codec;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.function.Function;

public enum Blend {
    ALPHA(alpha -> new Vector4f(1.0F, 1.0F, 1.0F, alpha), new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)),
    ADD(alpha -> new Vector4f(1.0F, 1.0F, 1.0F, alpha), new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)),
    SUBTRACT(alpha -> new Vector4f(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO)),
    MULTIPLY(alpha -> new Vector4f(alpha, alpha, alpha, alpha), new BlendFunction(SourceFactor.DST_COLOR, DestFactor.ONE_MINUS_SRC_ALPHA)),
    DODGE(alpha -> new Vector4f(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.ONE, DestFactor.ONE)),
    BURN(alpha -> new Vector4f(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.ZERO, DestFactor.ONE_MINUS_SRC_COLOR)),
    SCREEN(alpha -> new Vector4f(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_COLOR)),
    OVERLAY(alpha -> new Vector4f(alpha, alpha, alpha, 1.0F), new BlendFunction(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR)),
    REPLACE(alpha -> new Vector4f(1.0F, 1.0F, 1.0F, alpha), null);

    public static final Codec<Blend> CODEC = Codec.STRING.xmap(Blend::byName, Blend::toString);

    private final Function<Float, Vector4f> blendConsumer;
    private final BlendFunction blendFunction;

    Blend(Function<Float, Vector4f> blendConsumer, BlendFunction blendFunction) {
        this.blendConsumer = blendConsumer;
        this.blendFunction = blendFunction;
    }

    public static Blend byName(String name) {
        return Arrays.stream(Blend.values())
                .filter(blend -> blend.toString().toLowerCase().equals(name))
                .findFirst()
                .orElse(ADD);
    }

    public Vector4f getShaderColor(float value) {
        return this.blendConsumer.apply(value);
    }

    public BlendFunction getBlendFunction() {
        return this.blendFunction;
    }
}
