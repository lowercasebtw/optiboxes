package btw.lowercase.optiboxes.utils.components;

import btw.lowercase.optiboxes.utils.BlendFunction;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Blend {
    ALPHA(alpha -> RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha), new BlendFunction(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)),
    ADD(alpha -> RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha), new BlendFunction(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE)),
    SUBTRACT(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO)),
    MULTIPLY(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, alpha), new BlendFunction(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)),
    DODGE(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE)),
    BURN(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR)),
    SCREEN(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR)),
    OVERLAY(alpha -> RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F), new BlendFunction(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR)),
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
