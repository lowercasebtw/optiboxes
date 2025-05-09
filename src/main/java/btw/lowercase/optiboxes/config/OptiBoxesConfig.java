package btw.lowercase.optiboxes.config;

import btw.lowercase.optiboxes.OptiBoxesClient;
import btw.lowercase.optiboxes.utils.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class OptiBoxesConfig {
    private static OptiBoxesConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setStrictness(Strictness.STRICT).create();
    private static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve(OptiBoxesClient.MOD_ID + ".json");
    private static final Logger LOGGER = LoggerFactory.getLogger("OptiBoxesConfig");
    private static final OptiBoxesConfig DEFAULTS = new OptiBoxesConfig();

    public boolean enabled = true;
    public boolean processOptiFine = true;
    public boolean processMCPatcher = false;
    public boolean renderSunMoon = true;
    public boolean renderStars = true;
    public boolean showOverworldForUnknownDimension = true;

    public static void load() {
        instance = new OptiBoxesConfig();
        if (!CONFIG_FILE_PATH.toFile().exists()) {
            LOGGER.info("Config file doesn't exist! Creating one...");
            save();
            return;
        }

        try {
            String json = Files.readString(CONFIG_FILE_PATH);
            JsonObject object = GSON.fromJson(json, JsonObject.class);
            if (object == null) {
                LOGGER.warn("Failed to load config! Defaulting to original settings.");
            } else {
                instance.enabled = CommonUtils.getBooleanOr(object, "enabled", DEFAULTS.enabled);
                instance.processOptiFine = CommonUtils.getBooleanOr(object, "processOptiFine", DEFAULTS.processOptiFine);
                instance.processMCPatcher = CommonUtils.getBooleanOr(object, "processMCPatcher", DEFAULTS.processMCPatcher);
                instance.renderSunMoon = CommonUtils.getBooleanOr(object, "renderSunMoon", DEFAULTS.renderSunMoon);
                instance.renderStars = CommonUtils.getBooleanOr(object, "renderStars", DEFAULTS.renderStars);
                instance.showOverworldForUnknownDimension = CommonUtils.getBooleanOr(object, "showOverworldForUnknownDimension", DEFAULTS.showOverworldForUnknownDimension);
            }
        } catch (Exception ignored) {
            LOGGER.warn("Failed to load config! Error occured when reading file.");
            return;
        }

        LOGGER.info("Config successfully loaded!");
    }

    public static void save() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", instance.enabled);
        object.addProperty("processOptiFine", instance.processOptiFine);
        object.addProperty("processMCPatcher", instance.processMCPatcher);
        object.addProperty("renderSunMoon", instance.renderSunMoon);
        object.addProperty("renderStars", instance.renderStars);
        object.addProperty("showOverworldForUnknownDimension", instance.showOverworldForUnknownDimension);

        try {
            Files.write(CONFIG_FILE_PATH, GSON.toJson(object).getBytes());
        } catch (Exception ignored) {
            LOGGER.warn("Failed to save config!");
            return;
        }

        LOGGER.info("Config successfully saved!");
    }

    public static OptiBoxesConfig instance() {
        return instance;
    }
}