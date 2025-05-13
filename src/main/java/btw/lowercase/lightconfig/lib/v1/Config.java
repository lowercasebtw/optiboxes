package btw.lowercase.lightconfig.lib.v1;

import btw.lowercase.lightconfig.lib.v1.field.AbstractConfigField;
import btw.lowercase.lightconfig.lib.v1.field.BooleanConfigField;
import btw.lowercase.lightconfig.lib.v1.field.NumericConfigField;
import btw.lowercase.lightconfig.lib.v1.field.StringConfigField;
import com.google.gson.*;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class Config {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().setStrictness(Strictness.STRICT).create();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final List<AbstractConfigField<?>> configFields = new ArrayList<>();
    protected final ModContainer modContainer;
    protected final Path path;
    protected final ConfigSerializer<? extends JsonElement> serializer;

    public Config(ModContainer modContainer, Path path) {
        this.modContainer = modContainer;
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
        this.configFields.forEach(AbstractConfigField::restore);
        this.save();
    }

    public abstract Screen getConfigScreen(@Nullable Screen parent);

    public Logger getLogger() {
        return logger;
    }

    public List<AbstractConfigField<?>> getConfigFields() {
        return configFields;
    }

    public ModContainer getModContainer() {
        return this.modContainer;
    }

    public Path getPath() {
        return path;
    }
}
