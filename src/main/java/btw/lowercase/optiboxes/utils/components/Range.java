package btw.lowercase.optiboxes.utils.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public record Range(float min, float max) {
    public static final Codec<Range> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("min").forGetter(Range::min),
            Codec.FLOAT.fieldOf("max").forGetter(Range::max)
    ).apply(instance, Range::new));

    public Range {
        if (min > max) {
            throw new IllegalStateException("Maximum value is lower than the minimum value:\n" + this);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
