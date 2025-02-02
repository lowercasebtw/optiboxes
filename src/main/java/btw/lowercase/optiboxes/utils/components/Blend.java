package btw.lowercase.optiboxes.utils.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Blend {
    ALPHA(alpha -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    }),
    ADD(alpha -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    }),
    SUBTRACT(alpha -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    MULTIPLY(alpha -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(alpha, alpha, alpha, alpha);
    }),
    DODGE(alpha -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    BURN(alpha -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    SCREEN(alpha -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    OVERLAY(alpha -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR);
        RenderSystem.setShaderColor(alpha, alpha, alpha, 1.0F);
    }),
    REPLACE(alpha -> {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    });

    public static final Codec<Blend> CODEC = Codec.STRING.xmap(Blend::byName, Blend::toString);

    private final Consumer<Float> blendFunc;

    Blend(Consumer<Float> blendFunc) {
        this.blendFunc = blendFunc;
    }

    public Consumer<Float> getBlendFunc() {
        return this.blendFunc;
    }

    public static Blend byName(String name) {
        return Arrays.stream(Blend.values()).filter(blend -> blend.name().toLowerCase().equals(name)).findFirst().orElse(ADD);
    }
}
