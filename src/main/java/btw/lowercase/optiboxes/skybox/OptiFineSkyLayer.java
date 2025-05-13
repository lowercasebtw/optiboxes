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

public record OptiFineSkyLayer(ResourceLocation source, boolean biomeInclusion, List<ResourceLocation> biomes,
                               List<Range> heights, Blend blend, Fade fade, boolean rotate, float speed, Vector3f axis,
                               Loop loop, float transition, List<Weather> weatherConditions) {
    public static final Codec<OptiFineSkyLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("source").forGetter(OptiFineSkyLayer::source),
            Codec.BOOL.optionalFieldOf("biomeInclusion", true).forGetter(OptiFineSkyLayer::biomeInclusion),
            ResourceLocation.CODEC.listOf().optionalFieldOf("biomes", ImmutableList.of()).forGetter(OptiFineSkyLayer::biomes),
            Range.CODEC.listOf().optionalFieldOf("heights", ImmutableList.of()).forGetter(OptiFineSkyLayer::heights),
            Blend.CODEC.optionalFieldOf("blend", Blend.ADD).forGetter(OptiFineSkyLayer::blend),
            Fade.CODEC.optionalFieldOf("fade", Fade.DEFAULT).forGetter(OptiFineSkyLayer::fade),
            Codec.BOOL.optionalFieldOf("rotate", false).forGetter(OptiFineSkyLayer::rotate),
            Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(OptiFineSkyLayer::speed),
            ExtraCodecs.VECTOR3F.optionalFieldOf("axis", new Vector3f(1.0F, 0.0F, 0.0F)).forGetter(OptiFineSkyLayer::axis),
            Loop.CODEC.optionalFieldOf("loop", Loop.DEFAULT).forGetter(OptiFineSkyLayer::loop),
            Codec.FLOAT.optionalFieldOf("transition", 1.0F).forGetter(OptiFineSkyLayer::transition),
            Weather.CODEC.listOf().optionalFieldOf("weather", ImmutableList.of(Weather.CLEAR)).forGetter(OptiFineSkyLayer::weatherConditions)
    ).apply(instance, OptiFineSkyLayer::new));

    public boolean getConditionCheck(Level level) {
        final Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return false;
        }

        final BlockPos entityPos = cameraEntity.getOnPos();
        if (!this.biomes.isEmpty()) {
            final Holder<Biome> currentBiome = level.getBiome(entityPos);
            if (!currentBiome.isBound()) {
                return false;
            }

            if (!(this.biomeInclusion && this.biomes.contains(level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(currentBiome.value())))) {
                return false;
            }
        }

        return this.heights == null || CommonUtils.checkRanges(entityPos.getY(), this.heights);
    }

    public float getPositionBrightness(Level level, float conditionAlpha) {
        if (this.biomes().isEmpty() && this.heights().isEmpty()) {
            return 1.0F;
        }

        if (conditionAlpha == -1.0F) {
            return this.getConditionCheck(level) ? 1.0F : 0.0F;
        }

        return CommonUtils.calculateConditionAlphaValue(1.0F, 0.0F, conditionAlpha, (int) (this.transition() * 20), this.getConditionCheck(level));
    }

    public boolean isActive(long dayTime, int clampedTimeOfDay) {
        if (!this.fade.alwaysOn() && CommonUtils.isInTimeInterval(clampedTimeOfDay, this.fade.endFadeOut(), this.fade.startFadeIn())) {
            return false;
        } else if (this.loop.ranges() != null) {
            long adjustedTime = dayTime - (long) this.fade.startFadeIn();
            while (adjustedTime < 0L) {
                adjustedTime += 24000L * (int) this.loop.days();
            }

            final int daysPassed = (int) (adjustedTime / 24000L);
            final int currentDay = daysPassed % (int) this.loop.days();
            return CommonUtils.checkRanges(currentDay, this.loop.ranges());
        } else {
            return true;
        }
    }
}
