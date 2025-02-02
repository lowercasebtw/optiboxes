package btw.lowercase.optiboxes.utils.components;

import btw.lowercase.optiboxes.utils.CommonUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Fade(int startFadeIn, int endFadeIn, int startFadeOut, int endFadeOut, boolean alwaysOn) {
    public static final Fade DEFAULT = new Fade(0, 0, 0, 0, true);
    public static final Codec<Fade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("startFadeIn", 0).forGetter(Fade::startFadeIn),
            Codec.INT.optionalFieldOf("endFadeIn", 0).forGetter(Fade::endFadeIn),
            Codec.INT.optionalFieldOf("startFadeOut", 0).forGetter(Fade::startFadeOut),
            Codec.INT.optionalFieldOf("endFadeOut", 0).forGetter(Fade::endFadeOut),
            Codec.BOOL.optionalFieldOf("alwaysOn", false).forGetter(Fade::alwaysOn)
    ).apply(instance, Fade::new));

    public Fade(int startFadeIn, int endFadeIn, int startFadeOut, int endFadeOut, boolean alwaysOn) {
        this.startFadeIn = normalizeIfNot(startFadeIn, alwaysOn);
        this.endFadeIn = normalizeIfNot(endFadeIn, alwaysOn);
        this.startFadeOut = normalizeIfNot(startFadeOut, alwaysOn);
        this.endFadeOut = normalizeIfNot(endFadeOut, alwaysOn);
        this.alwaysOn = alwaysOn;
    }

    private static int normalizeIfNot(int time, boolean ignore) {
        return ignore ? time : CommonUtils.normalizeTickTime(time);
    }
}