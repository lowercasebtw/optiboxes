package btw.lowercase.lightconfig.lib.field;

import btw.lowercase.lightconfig.lib.Config;
import btw.lowercase.lightconfig.lib.ConfigTranslate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class BooleanConfigField extends ConfigField<Boolean> {
    private boolean enabled;

    public BooleanConfigField(final Config config, final String name, final boolean defaultValue) {
        super(config, name, defaultValue);
        this.enabled = true;
    }

    @Override
    public void load(JsonObject object) throws Exception {
        if (!object.has(this.name)) {
            throw new Exception("Failed to load value for '" + this.name + "', object didn't contain a value for it.");
        } else {
            final JsonElement element = object.get(this.name);
            if (!element.isJsonPrimitive() || (element instanceof JsonPrimitive primitive && !primitive.isBoolean())) {
                throw new Exception("Failed to load value for '" + this.name + "', type does not match.");
            } else {
                this.setEnabled(element.getAsBoolean());
            }
        }
    }

    @Override
    public void save(JsonObject object) {
        object.addProperty(this.name, this.enabled);
    }

    @Override
    public void restore() {
        this.setEnabled(this.getDefaultValue());
    }

    @Override
    public Button createWidget() {
        return this.createWidget(() -> {
        });
    }

    public Button createWidget(Runnable onClick) {
        final String translate = String.format("options." + this.config.getModId() + ".%s", this.getName());
        final Component name = Component.translatable(translate);
        return Button.builder(ConfigTranslate.TEMPLATE.apply(name, this.isEnabled() ? ConfigTranslate.ON : ConfigTranslate.OFF), (button) -> {
                    this.toggle();
                    onClick.run();
                    button.setMessage(ConfigTranslate.TEMPLATE.apply(name, this.isEnabled() ? ConfigTranslate.ON : ConfigTranslate.OFF));
                })
                .tooltip(Tooltip.create(ConfigTranslate.tooltip(translate)))
                .build();
    }

    public void toggle() {
        this.setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}
