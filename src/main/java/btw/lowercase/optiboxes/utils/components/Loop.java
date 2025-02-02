package btw.lowercase.optiboxes.utils.components;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import btw.lowercase.optiboxes.utils.CommonUtils;

import java.util.List;

public record Loop(double days, List<Range> ranges) {
    public static final Loop DEFAULT = new Loop(7, ImmutableList.of());
    public static final Codec<Loop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CommonUtils.getClampedDoubleCodec(1, Double.MAX_VALUE).optionalFieldOf("days", 7.0D).forGetter(Loop::days),
            Range.CODEC.listOf().optionalFieldOf("ranges", ImmutableList.of()).forGetter(Loop::ranges)
    ).apply(instance, Loop::new));
}