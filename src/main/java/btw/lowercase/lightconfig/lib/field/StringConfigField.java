package btw.lowercase.lightconfig.lib.field;

import btw.lowercase.lightconfig.lib.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class StringConfigField extends GenericConfigField<String> {
    public StringConfigField(final Config config, final String name, final String defaultValue) {
        super(config, name, defaultValue);
    }

    @Override
    public void load(JsonObject object) throws Exception {
        if (!object.has(this.name)) {
            throw new Exception("Failed to load value for '" + this.name + "', object didn't contain a value for it.");
        } else {
            final JsonElement element = object.get(this.name);
            if (!element.isJsonPrimitive() || (element instanceof final JsonPrimitive primitive && !primitive.isString())) {
                throw new Exception("Failed to load value for '" + this.name + "', type does not match.");
            } else {
                this.setValue(element.getAsString());
            }
        }
    }

    @Override
    public void save(JsonObject object) {
        object.addProperty(this.name, this.value);
    }
}
