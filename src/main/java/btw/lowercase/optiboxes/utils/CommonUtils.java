package btw.lowercase.optiboxes.utils;

import btw.lowercase.optiboxes.utils.components.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class CommonUtils {
    private static final Pattern OPTIFINE_RANGE_SEPARATOR = Pattern.compile("(\\d|\\))-(\\d|\\()");

    public static JsonObject convertOptiFineSkyProperties(OptiFineResourceHelper optiFineResourceHelper, Properties properties, ResourceLocation propertiesResourceLocation) {
        JsonObject jsonObject = new JsonObject();
        ResourceLocation sourceTexture = parseSourceTexture(properties.getProperty("source", null), optiFineResourceHelper, propertiesResourceLocation);
        if (sourceTexture == null) {
            return null;
        } else {
            jsonObject.addProperty("source", sourceTexture.toString());
        }

        // Blend
        if (properties.containsKey("blend")) {
            String blend = properties.getProperty("blend");
            if (blend != null) {
                jsonObject.addProperty("blend", blend);
            }
        }

        // Convert fade
        JsonObject fade = new JsonObject();
        if (properties.containsKey("startFadeIn") && properties.containsKey("endFadeIn") && properties.containsKey("endFadeOut")) {
            int startFadeIn = Objects.requireNonNull(CommonUtils.toTickTime(properties.getProperty("startFadeIn"))).intValue();
            int endFadeIn = Objects.requireNonNull(CommonUtils.toTickTime(properties.getProperty("endFadeIn"))).intValue();
            int endFadeOut = Objects.requireNonNull(CommonUtils.toTickTime(properties.getProperty("endFadeOut"))).intValue();
            int startFadeOut;
            if (properties.containsKey("startFadeOut")) {
                startFadeOut = Objects.requireNonNull(CommonUtils.toTickTime(properties.getProperty("startFadeOut"))).intValue();
            } else {
                startFadeOut = endFadeOut - (endFadeIn - startFadeIn);
                if (startFadeIn <= startFadeOut && endFadeIn >= startFadeOut) {
                    startFadeOut = endFadeOut;
                }
            }

            fade.addProperty("startFadeIn", CommonUtils.normalizeTickTime(startFadeIn));
            fade.addProperty("endFadeIn", CommonUtils.normalizeTickTime(endFadeIn));
            fade.addProperty("startFadeOut", CommonUtils.normalizeTickTime(startFadeOut));
            fade.addProperty("endFadeOut", CommonUtils.normalizeTickTime(endFadeOut));
        } else {
            fade.addProperty("alwaysOn", true);
        }

        jsonObject.add("fade", fade);

        // Speed
        if (properties.containsKey("speed")) {
            float speed = Float.parseFloat(properties.getProperty("speed", "1")) * -1;
            jsonObject.addProperty("speed", speed);
        }

        // Rotation
        if (properties.containsKey("rotate")) {
            jsonObject.addProperty("rotate", Boolean.parseBoolean(properties.getProperty("rotate", "true")));
        }

        // Transition
        if (properties.containsKey("transition")) {
            jsonObject.addProperty("transition", Integer.parseInt(properties.getProperty("transition", "1")));
        }

        // Axis
        JsonArray jsonAxis = new JsonArray();
        if (properties.containsKey("axis")) {
            String[] axis = properties.getProperty("axis").trim().replaceAll(" +", " ").split(" ");
            List<String> rev = Arrays.asList(axis);
            axis = rev.toArray(axis);
            Arrays.stream(axis).forEach(a -> jsonAxis.add(Float.parseFloat(a)));
            jsonObject.add("axis", jsonAxis);
        }

        // Weather
        if (properties.containsKey("weather")) {
            String[] weathers = properties.getProperty("weather").split(" ");
            JsonArray jsonWeather = new JsonArray();
            if (weathers.length > 0) {
                Arrays.stream(weathers).forEach(jsonWeather::add);
            } else {
                jsonWeather.add("clear");
            }

            jsonObject.add("weathers", jsonWeather);
        }

        // Biomes
        if (properties.containsKey("biomes")) {
            String biomesString = properties.getProperty("biomes");
            if (biomesString.startsWith("!")) {
                jsonObject.addProperty("biomeInclusion", false);
                biomesString = biomesString.substring(1);
            }

            String[] biomes = biomesString.split(" ");
            if (biomes.length > 0) {
                JsonArray jsonBiomes = new JsonArray();
                Arrays.stream(biomes).filter(ResourceLocation::isValidPath).forEach(jsonBiomes::add);
                jsonObject.add("biomes", jsonBiomes);
            }
        }

        // Heights
        if (properties.containsKey("heights")) {
            List<Range> rangeEntries = CommonUtils.parseRangeEntriesNegative(properties.getProperty("heights"));
            if (!rangeEntries.isEmpty()) {
                JsonArray jsonYRanges = new JsonArray();
                rangeEntries.forEach(range -> {
                    JsonObject rangeObj = new JsonObject();
                    rangeObj.addProperty("min", range.min());
                    rangeObj.addProperty("max", range.max());
                    jsonYRanges.add(rangeObj);
                });
                jsonObject.add("heights", jsonYRanges);
            }
        }

        // Days Loop -> Loop
        if (properties.containsKey("days")) {
            List<Range> rangeEntries = CommonUtils.parseRangeEntries(properties.getProperty("days"));
            if (!rangeEntries.isEmpty()) {
                JsonObject loopObject = new JsonObject();
                JsonArray loopRange = new JsonArray();
                rangeEntries.forEach(range -> {
                    JsonObject rangeObj = new JsonObject();
                    rangeObj.addProperty("min", range.min());
                    rangeObj.addProperty("max", range.max());
                    loopRange.add(rangeObj);
                });

                int value = 8;
                if (properties.containsKey("daysLoop")) {
                    value = CommonUtils.parseInt(properties.getProperty("daysLoop"), 8);
                }

                loopObject.addProperty("days", value);
                loopObject.add("ranges", loopRange);
                jsonObject.add("loop", loopObject);
            }
        }

        return jsonObject;
    }

    public static ResourceLocation parseSourceTexture(String source, OptiFineResourceHelper optiFineResourceHelper, ResourceLocation propertiesId) {
        ResourceLocation textureId;
        String namespace;
        String path;
        if (source == null) {
            namespace = propertiesId.getNamespace();
            path = propertiesId.getPath().replace(".properties", ".png");
        } else {
            if (source.startsWith("./")) {
                namespace = propertiesId.getNamespace();
                String fileName = propertiesId.getPath().split("/")[propertiesId.getPath().split("/").length - 1];
                path = propertiesId.getPath().replace(fileName, source.substring(2));
            } else {
                String[] parts = source.split("/", 3);
                if (parts.length == 3 && parts[0].equals("assets")) {
                    namespace = parts[1];
                    path = parts[2];
                } else {
                    ResourceLocation sourceResourceLocation = ResourceLocation.tryParse(source);
                    if (sourceResourceLocation != null) {
                        namespace = sourceResourceLocation.getNamespace();
                        path = sourceResourceLocation.getPath();
                    } else {
                        return null;
                    }
                }
            }
        }

        try {
            textureId = ResourceLocation.fromNamespaceAndPath(namespace, path);
        } catch (ResourceLocationException e) {
            return null;
        }

        InputStream textureInputStream = optiFineResourceHelper.getInputStream(textureId);
        if (textureInputStream == null) {
            return null;
        }

        try {
            textureInputStream.close();
        } catch (Exception ignored) {
        }

        return textureId;
    }

    public static Number toTickTime(String time) {
        String[] parts = time.split(":");
        if (parts.length == 2) {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            return h * 1000 + (m / 0.06F) - 6000;
        } else {
            return null;
        }
    }

    public static int normalizeTickTime(int tickTime) {
        int result = tickTime % 24000;
        if (result < 0) {
            result += 24000;
        }

        return result;
    }

    public static List<Range> parseRangeEntries(String source) {
        List<Range> rangeEntries = new ArrayList<>();
        String[] parts = source.trim().split(" ");
        for (String part : parts) {
            Range range = parseRangeEntry(part);
            if (range != null) {
                rangeEntries.add(range);
            }
        }

        return rangeEntries;
    }

    private static Range parseRangeEntry(String part) {
        if (part != null) {
            if (part.contains("-")) {
                String[] parts = part.split("-");
                if (parts.length == 2) {
                    int min = parseInt(parts[0], -1);
                    int max = parseInt(parts[1], -1);
                    if (min >= 0 && max >= 0) {
                        return new Range(min, max);
                    }
                }
            } else {
                int value = parseInt(part, -1);
                if (value >= 0) {
                    return new Range(value, value);
                }
            }
        }

        return null;
    }

    public static List<Range> parseRangeEntriesNegative(String source) {
        List<Range> rangeEntries = new ArrayList<>();
        String[] parts = source.trim().split(" ");
        for (String part : parts) {
            Range range = parseRangeEntryNegative(part);
            if (range != null) {
                rangeEntries.add(range);
            }
        }

        return rangeEntries;
    }

    private static Range parseRangeEntryNegative(String part) {
        if (part != null) {
            String s = OPTIFINE_RANGE_SEPARATOR.matcher(part).replaceAll("$1=$2");
            if (s.contains("=")) {
                String[] parts = s.split("=");
                if (parts.length == 2) {
                    int j = parseInt(stripBrackets(parts[0]), Integer.MIN_VALUE);
                    int k = parseInt(stripBrackets(parts[1]), Integer.MIN_VALUE);
                    if (j != Integer.MIN_VALUE && k != Integer.MIN_VALUE) {
                        int min = Math.min(j, k);
                        int max = Math.max(j, k);
                        return new Range(min, max);
                    }
                }
            } else {
                int i = parseInt(stripBrackets(part), Integer.MIN_VALUE);
                if (i != Integer.MIN_VALUE) {
                    return new Range(i, i);
                }
            }
        }
        return null;
    }

    private static String stripBrackets(String str) {
        return str.startsWith("(") && str.endsWith(")") ? str.substring(1, str.length() - 1) : str;
    }

    public static int parseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean checkRanges(double value, List<Range> rangeEntries) {
        return rangeEntries.isEmpty() || rangeEntries.stream()
                .anyMatch(range -> com.google.common.collect.Range.closed(range.min(), range.max()).contains((float) value));
    }

    public static boolean isInTimeInterval(int currentTime, int startTime, int endTime) {
        if (currentTime < 0 || currentTime >= 24000) {
            return false; // Invalid time
        } else if (startTime <= endTime) {
            return currentTime >= startTime && currentTime <= endTime;
        } else {
            return currentTime >= startTime || currentTime <= endTime;
        }
    }

    public static float calculateFadeAlphaValue(float maxAlpha, float minAlpha, int currentTime, int startFadeIn, int endFadeIn, int startFadeOut, int endFadeOut) {
        if (isInTimeInterval(currentTime, endFadeIn, startFadeOut)) {
            return maxAlpha;
        } else if (isInTimeInterval(currentTime, startFadeIn, endFadeIn)) {
            int fadeInDuration = calculateCyclicTimeDistance(startFadeIn, endFadeIn);
            int timePassedSinceFadeInStart = calculateCyclicTimeDistance(startFadeIn, currentTime);
            return minAlpha + ((float) timePassedSinceFadeInStart / fadeInDuration) * (maxAlpha - minAlpha);
        } else if (isInTimeInterval(currentTime, startFadeOut, endFadeOut)) {
            int fadeOutDuration = calculateCyclicTimeDistance(startFadeOut, endFadeOut);
            int timePassedSinceFadeOutStart = calculateCyclicTimeDistance(startFadeOut, currentTime);
            return maxAlpha + ((float) timePassedSinceFadeOutStart / fadeOutDuration) * (minAlpha - maxAlpha);
        } else {
            return minAlpha;
        }
    }

    public static int calculateCyclicTimeDistance(int startTime, int endTime) {
        return (endTime - startTime + 24000) % 24000;
    }

    public static float calculateConditionAlphaValue(float maxAlpha, float minAlpha, float lastAlpha, int duration, boolean in) {
        if (duration == 0) {
            return lastAlpha;
        } else if (in && maxAlpha == lastAlpha) {
            return maxAlpha;
        } else if (!in && lastAlpha == minAlpha) {
            return minAlpha;
        } else {
            float alphaChange = (maxAlpha - minAlpha) / duration;
            float result = in ? lastAlpha + alphaChange : lastAlpha - alphaChange;
            return Mth.clamp(result, minAlpha, maxAlpha);
        }
    }

    public static Codec<Double> getClampedDoubleCodec(double min, double max) {
        if (min > max) {
            throw new UnsupportedOperationException("Maximum value was lesser than than the minimum value");
        } else {
            return Codec.DOUBLE.xmap(f -> Mth.clamp(f, min, max), Function.identity());
        }
    }
}
