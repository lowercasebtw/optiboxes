package btw.lowercase.lightconfig.lib.field;

import btw.lowercase.lightconfig.lib.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class BooleanConfigField extends ConfigField<Boolean> {
    private boolean enabled;

    public BooleanConfigField(final Config config, final String name, final Boolean defaultValue) {
        super(config, name, defaultValue);
        this.enabled = true;
    }

    @Override
    public void load(JsonObject object) throws Exception {
        if (!object.has(this.name)) {
            throw new Exception("Failed to load value for '" + this.name + "', object didn't contain a value for it.");
        } else {
            JsonElement element = object.get(this.name);
            if (!element.isJsonPrimitive() || (element instanceof JsonPrimitive primitive && !primitive.isBoolean())) {
                throw new Exception("Failed to load value for '" + this.name + "', type does not match.");
            } else {
                this.enabled = element.getAsBoolean();
            }
        }
    }

    @Override
    public void save(JsonObject object) {
        object.addProperty(this.name, this.enabled);
    }

    public BooleanConfigField toggle() {
        this.enabled = !this.enabled;
        return this;
    }

    public BooleanConfigField setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}
