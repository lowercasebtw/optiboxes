package btw.lowercase.lightconfig.lib.v1.field;

import btw.lowercase.lightconfig.lib.v1.Config;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.components.AbstractWidget;

public class GenericConfigField<T> extends AbstractConfigField<T> {
    protected T value;

    public GenericConfigField(Config config, String name, T defaultValue) {
        super(config, name, defaultValue);
        this.value = defaultValue;
    }

    @Override
    public void load(JsonObject object) throws Exception {
        throw new RuntimeException("Unimplemented load for " + this.getClass().getName());
    }

    @Override
    public void save(JsonObject object) throws Exception {
        throw new RuntimeException("Unimplemented save for " + this.getClass().getName());
    }

    @Override
    public void restore() {
        this.setValue(this.getDefaultValue());
    }

    @Override
    public AbstractWidget createWidget() {
        throw new RuntimeException("Unimplemented createWidget for " + this.getClass().getName());
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
