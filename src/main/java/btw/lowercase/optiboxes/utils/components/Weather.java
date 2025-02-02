package btw.lowercase.optiboxes.utils.components;

import com.mojang.serialization.Codec;

import java.util.Arrays;

public enum Weather {
    CLEAR,
    RAIN,
    RAIN_BIOME,
    SNOW,
    THUNDER;

    public static final Codec<Weather> CODEC = Codec.STRING.xmap(Weather::byName, Weather::toString);

    public static Weather byName(String name) {
        return Arrays.stream(Weather.values()).filter(weather -> weather.toString().toLowerCase().equals(name)).findFirst().orElse(null);
    }
}
