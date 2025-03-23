package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.utils.CommonUtils;
import btw.lowercase.optiboxes.utils.components.*;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3f;

import java.util.List;

public final class OptiFineSkyLayer {
    public static final Codec<OptiFineSkyLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("source").forGetter(OptiFineSkyLayer::getSource),
            Codec.BOOL.optionalFieldOf("biomeInclusion", true).forGetter(OptiFineSkyLayer::isBiomeInclusion),
            ResourceLocation.CODEC.listOf().optionalFieldOf("biomes", ImmutableList.of()).forGetter(OptiFineSkyLayer::getBiomes),
            Range.CODEC.listOf().optionalFieldOf("heights", ImmutableList.of()).forGetter(OptiFineSkyLayer::getHeights),
            Blend.CODEC.optionalFieldOf("blend", Blend.ADD).forGetter(OptiFineSkyLayer::getBlend),
            Fade.CODEC.optionalFieldOf("fade", Fade.DEFAULT).forGetter(OptiFineSkyLayer::getFade),
            Codec.BOOL.optionalFieldOf("rotate", false).forGetter(OptiFineSkyLayer::shouldRotate),
            Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(OptiFineSkyLayer::getSpeed),
            ExtraCodecs.VECTOR3F.optionalFieldOf("axis", new Vector3f(1.0F, 0.0F, 0.0F)).forGetter(OptiFineSkyLayer::getAxis),
            Loop.CODEC.optionalFieldOf("loop", Loop.DEFAULT).forGetter(OptiFineSkyLayer::getLoop),
            Codec.FLOAT.optionalFieldOf("transition", 1.0F).forGetter(OptiFineSkyLayer::getTransition),
            Weather.CODEC.listOf().optionalFieldOf("weather", ImmutableList.of(Weather.CLEAR)).forGetter(OptiFineSkyLayer::getWeatherConditions)
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
    private final List<Weather> weatherConditions;

    public float conditionAlpha = -1;

    public OptiFineSkyLayer(ResourceLocation source, boolean biomeInclusion, List<ResourceLocation> biomes, List<Range> heights, Blend blend, Fade fade, boolean rotate, float speed, Vector3f axis, Loop loop, float transition, List<Weather> weatherConditions) {
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
        this.weatherConditions = weatherConditions;
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

    public boolean shouldRotate() {
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

    public List<Weather> getWeatherConditions() {
        return weatherConditions;
    }

    public void setConditionAlpha(float conditionAlpha) {
        this.conditionAlpha = conditionAlpha;
    }
}
