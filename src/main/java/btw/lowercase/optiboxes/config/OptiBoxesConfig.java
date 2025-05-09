package btw.lowercase.optiboxes.config;

import btw.lowercase.lightconfig.lib.Config;
import btw.lowercase.lightconfig.lib.field.BooleanConfigField;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class OptiBoxesConfig extends Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("OptiBoxesConfig");

    public BooleanConfigField enabled = this.booleanFieldOf("enabled", true);
    public BooleanConfigField processOptiFine = this.booleanFieldOf("processOptiFine", true);
    public BooleanConfigField processMCPatcher = this.booleanFieldOf("processMCPatcher", false);
    public BooleanConfigField renderSunMoon = this.booleanFieldOf("renderSunMoon", true);
    public BooleanConfigField renderStars = this.booleanFieldOf("renderStars", true);
    public BooleanConfigField showOverworldForUnknownDimension = this.booleanFieldOf("showOverworldForUnknownDimension", true);

    public OptiBoxesConfig(Path path) {
        super(path);
    }

    @Override
    public void load() {
        if (!this.path.toFile().exists()) {
            LOGGER.info("Config file doesn't exist! Creating one...");
            this.save();
            return;
        }

        try {
            final String json = Files.readString(this.path);
            final JsonObject object = GSON.fromJson(json, JsonObject.class);
            if (object == null) {
                LOGGER.warn("Failed to load config! Defaulting to original settings.");
            } else {
                this.configFields.forEach(field -> {
                    try {
                        field.load(object);
                    } catch (Exception ignored) {
                        LOGGER.warn("Failed to load config field '{}', setting it to default value!", field.getName());
                    }
                });
            }
        } catch (Exception ignored) {
            LOGGER.warn("Failed to load config! Error occured when reading file.");
            return;
        }

        LOGGER.info("Config successfully loaded!");
    }

    @Override
    public void save() {
        final JsonObject object = new JsonObject();
        this.configFields.forEach(field -> {
            try {
                field.save(object);
            } catch (Exception ignored) {
                LOGGER.warn("Failed to save config field '{}'!", field.getName());
            }
        });

        try {
            Files.write(this.path, GSON.toJson(object).getBytes());
        } catch (Exception ignored) {
            LOGGER.warn("Failed to save config!");
            return;
        }

        LOGGER.info("Config successfully saved!");
    }
}