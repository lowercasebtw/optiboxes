package btw.lowercase.optiboxes.utils.components;

import btw.lowercase.optiboxes.utils.CommonUtils;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Blend {
    ALPHA(alpha -> {
        CommonUtils.enableBlend();
        CommonUtils.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    }),
    ADD(alpha -> {
        CommonUtils.enableBlend();
        CommonUtils.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    }),
    SUBTRACT(alpha -> {
        CommonUtils.enableBlend();
        CommonUtils.blendFunc(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    MULTIPLY(alpha -> {
        CommonUtils.enableBlend();
        CommonUtils.blendFunc(SourceFactor.DST_COLOR, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(alpha, alpha, alpha, alpha);
    }),
    DODGE(alpha -> {
        CommonUtils.enableBlend();
        CommonUtils.blendFunc(SourceFactor.ONE, DestFactor.ONE);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    BURN(alpha -> {
        CommonUtils.enableBlend();
        CommonUtils.blendFunc(SourceFactor.ZERO, DestFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    SCREEN(alpha -> {
        CommonUtils.enableBlend();
        CommonUtils.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    OVERLAY(alpha -> {
        CommonUtils.enableBlend();
        CommonUtils.blendFunc(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    REPLACE(alpha -> {
        CommonUtils.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    });

    public static final Codec<Blend> CODEC = Codec.STRING.xmap(Blend::byName, Blend::toString);

    private final Consumer<Float> blendConsumer;

    Blend(Consumer<Float> blendConsumer) {
        this.blendConsumer = blendConsumer;
    }

    public void apply(float value) {
        this.blendConsumer.accept(value);
    }

    public static Blend byName(String name) {
        return Arrays.stream(Blend.values()).filter(blend -> blend.toString().toLowerCase().equals(name)).findFirst().orElse(ADD);
    }
}
