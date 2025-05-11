package btw.lowercase.lightconfig.lib.field;

import btw.lowercase.lightconfig.lib.Config;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract class ConfigField<T> {
    protected final Config config;
    protected final String name;
    protected final T defaultValue;

    public ConfigField(final Config config, final String name, final T defaultValue) {
        this.config = config;
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public abstract void load(JsonObject object) throws Exception;

    public abstract void save(JsonObject object) throws Exception;

    public abstract void restore();

    public abstract AbstractWidget createWidget();

    public Component getTranslate() {
        return Component.translatable(String.format("options.%s.%s", this.config.getModContainer().getMetadata().getId(), this.getName()));
    }

    public String getName() {
        return name;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }
}