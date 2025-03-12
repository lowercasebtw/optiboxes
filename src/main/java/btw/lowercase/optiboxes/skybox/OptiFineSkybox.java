package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.config.OptiBoxesConfig;
import btw.lowercase.optiboxes.utils.components.Blend;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public class OptiFineSkybox {
    public static final Codec<OptiFineSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OptiFineSkyLayer.CODEC.listOf().optionalFieldOf("layers", ImmutableList.of()).forGetter(OptiFineSkybox::getLayers),
            Level.RESOURCE_KEY_CODEC.fieldOf("world").forGetter(OptiFineSkybox::getWorldResourceKey)
    ).apply(instance, OptiFineSkybox::new));

    private final List<OptiFineSkyLayer> layers;
    private final ResourceKey<Level> worldResourceKey;
    private boolean active = true;

    public OptiFineSkybox(List<OptiFineSkyLayer> layers, ResourceKey<Level> worldResourceKey) {
        this.layers = layers;
        this.worldResourceKey = worldResourceKey;
    }

    public void render(PoseStack poseStack, Level level, float tickDelta) {
        long dayTime = level.getDayTime();
        int clampedTimeOfDay = (int) (dayTime % 24000L);
        float skyAngle = level.getTimeOfDay(tickDelta);
        float thunderLevel = level.getThunderLevel(tickDelta);
        float rainLevel = level.getRainLevel(tickDelta);
        if (rainLevel > 0.0F) {
            thunderLevel /= rainLevel;
        }

        for (OptiFineSkyLayer optiFineSkyLayer : this.layers.stream().filter(layer -> layer.isActive(dayTime, clampedTimeOfDay)).toList()) {
            optiFineSkyLayer.render(level, poseStack, clampedTimeOfDay, skyAngle, rainLevel, thunderLevel);
        }

        Blend.ADD.apply(1.0F - rainLevel);
    }

    public void tick(ClientLevel level) {
        this.active = true;
        if (level.dimension().equals(this.worldResourceKey) || (OptiBoxesConfig.instance().showOverworldForUnknownDimension && this.worldResourceKey.equals(Level.OVERWORLD) && !level.dimension().equals(Level.NETHER) && !level.dimension().equals(Level.END))) {
            this.layers.forEach(layer -> layer.tick(level));
        } else {
            this.layers.forEach(layer -> layer.setConditionAlpha(-1.0F));
            this.active = false;
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public List<OptiFineSkyLayer> getLayers() {
        return layers;
    }

    public ResourceKey<Level> getWorldResourceKey() {
        return worldResourceKey;
    }
}
