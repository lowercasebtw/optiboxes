package btw.lowercase.lightconfig.lib.field;

import btw.lowercase.lightconfig.lib.Config;
import com.google.gson.JsonObject;

public class GenericConfigField<T> extends ConfigField<T> {
    private T value;

    public GenericConfigField(Config config, String name, T defaultValue) {
        super(config, name, defaultValue);
        this.value = defaultValue;
    }

    @Override
    public void load(JsonObject object) throws Exception {
        throw new RuntimeException("Unsupported operation for now");
    }

    @Override
    public void save(JsonObject object) throws Exception {
        throw new RuntimeException("Unsupported operation for now");
    }

    public T getValue() {
        return value;
    }

    public ConfigField<T> setValue(T value) {
        this.value = value;
        return this;
    }
}
