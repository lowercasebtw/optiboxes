package btw.lowercase.lightconfig.lib.v1.field;

import btw.lowercase.lightconfig.lib.v1.Config;
import btw.lowercase.lightconfig.lib.ConfigTranslate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class BooleanConfigField extends GenericConfigField<Boolean> {
    public BooleanConfigField(final Config config, final String name, final boolean defaultValue) {
        super(config, name, defaultValue);
    }

    @Override
    public void load(JsonObject object) throws Exception {
        if (!object.has(this.name)) {
            throw new Exception("Failed to load value for '" + this.name + "', object didn't contain a value for it.");
        } else {
            final JsonElement element = object.get(this.name);
            if (!element.isJsonPrimitive() || (element instanceof final JsonPrimitive primitive && !primitive.isBoolean())) {
                throw new Exception("Failed to load value for '" + this.name + "', type does not match.");
            } else {
                this.setValue(element.getAsBoolean());
            }
        }
    }

    @Override
    public void save(JsonObject object) {
        object.addProperty(this.name, this.value);
    }

    @Override
    public Button createWidget() {
        return this.createWidget(() -> {
        });
    }

    public Button createWidget(final Runnable onClick) {
        final String translationKey = this.getTranslationKey();
        final Component translate = Component.translatable(translationKey);
        return Button.builder(ConfigTranslate.TEMPLATE.apply(translate, ConfigTranslate.toggle(this.isEnabled())), (button) -> {
                    this.toggle();
                    onClick.run();
                    button.setMessage(ConfigTranslate.TEMPLATE.apply(translate, ConfigTranslate.toggle(this.isEnabled())));
                })
                .tooltip(Tooltip.create(ConfigTranslate.tooltip(translationKey)))
                .build();
    }

    public void toggle() {
        this.setValue(!this.value);
    }

    public boolean isEnabled() {
        return this.value;
    }
}
