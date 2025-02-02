package btw.lowercase.optiboxes.utils.components;

import com.mojang.serialization.Codec;

import java.util.Arrays;

public enum Weather {
    CLEAR("clear"),
    RAIN("rain"),
    BIOME_RAIN("rain_biome"),
    SNOW("snow"),
    THUNDER("thunder");

    public static final Codec<Weather> CODEC = Codec.STRING.xmap(Weather::byName, Weather::toString);

    private final String name;

    Weather(String name) {
        this.name = name;
    }

    public static Weather byName(String name) {
        return Arrays.stream(Weather.values()).filter(weather -> weather.name.equals(name)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
