package btw.lowercase.lightconfig.lib;

import btw.lowercase.lightconfig.lib.field.BooleanConfigField;
import btw.lowercase.lightconfig.lib.field.ConfigField;
import btw.lowercase.lightconfig.lib.field.NumericConfigField;
import btw.lowercase.lightconfig.lib.field.StringConfigField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class Config {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().setStrictness(Strictness.STRICT).create();
    protected final List<ConfigField<?>> configFields = new ArrayList<>();
    protected final Path path;

    public Config(Path path) {
        this.path = path;
    }

    public BooleanConfigField booleanFieldOf(final String name, final Boolean defaultValue) {
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

    public abstract void load();

    public abstract void save();

    public List<ConfigField<?>> getConfigFields() {
        return configFields;
    }

    public Path getPath() {
        return path;
    }
}
