package btw.lowercase.lightconfig.lib;

import btw.lowercase.lightconfig.lib.field.BooleanConfigField;
import btw.lowercase.lightconfig.lib.field.ConfigField;
import btw.lowercase.lightconfig.lib.field.NumericConfigField;
import btw.lowercase.lightconfig.lib.field.StringConfigField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().setStrictness(Strictness.STRICT).create();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final List<ConfigField<?>> configFields = new ArrayList<>();
    protected final String modId;
    protected final Path path;
    protected final ConfigSerializer<JsonObject> serializer;

    public Config(String modId, Path path) {
        this.modId = modId;
        this.path = path;
        this.serializer = new ConfigSerializer<>();
    }

    public BooleanConfigField booleanFieldOf(final String name, final boolean defaultValue) {
        final BooleanConfigField field = new BooleanConfigField(this, name, defaultValue);
        this.configFields.add(field);
        return field;
    }

    public StringConfigField stringFieldOf(final String name, final String defaultValue) {
        final StringConfigField field = new StringConfigField(this, name, defaultValue);
        this.configFields.add(field);
        return field;
    }

    public <T extends Number> NumericConfigField<T> numericFieldOf(final String name, final T defaultValue) {
        final NumericConfigField<T> field = new NumericConfigField<>(this, name, defaultValue);
        this.configFields.add(field);
        return field;
    }

    public void load() {
        if (!this.path.toFile().exists()) {
            this.logger.info("Config file doesn't exist! Creating one...");
            this.save();
            return;
        }

        try {
            final String json = Files.readString(this.path);
            final JsonObject object = GSON.fromJson(json, JsonObject.class);
            if (object == null) {
                this.logger.warn("Failed to load config! Defaulting to original settings.");
            } else {
                this.configFields.forEach(field -> {
                    try {
                        field.load(object);
                    } catch (Exception ignored) {
                        this.logger.warn("Failed to load config field '{}', setting it to default value!", field.getName());
                    }
                });
            }
        } catch (Exception ignored) {
            this.logger.warn("Failed to load config! Error occured when reading file.");
            return;
        }

        this.logger.info("Config successfully loaded!");
    }

    public void save() {
        final JsonObject object = new JsonObject();
        this.configFields.forEach(field -> {
            try {
                field.save(object);
            } catch (Exception ignored) {
                this.logger.warn("Failed to save config field '{}'!", field.getName());
            }
        });

        try {
            Files.write(this.path, GSON.toJson(object).getBytes());
        } catch (Exception ignored) {
            this.logger.warn("Failed to save config!");
            return;
        }

        this.logger.info("Config successfully saved!");
    }

    public void reset() {
        // TODO: When implementing the screen system/idk, implement a event listener for like reload resource packs or whatever
        this.configFields.forEach(ConfigField::restore);
        this.save();
    }

    public Logger getLogger() {
        return logger;
    }

    public List<ConfigField<?>> getConfigFields() {
        return configFields;
    }

    public String getModId() {
        return modId;
    }

    public Path getPath() {
        return path;
    }
}
