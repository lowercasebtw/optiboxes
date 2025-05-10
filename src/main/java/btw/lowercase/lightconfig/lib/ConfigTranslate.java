package btw.lowercase.lightconfig.lib;

import net.minecraft.network.chat.Component;

import java.util.function.BiFunction;

public final class ConfigTranslate {
    public static final Component RESET = Component.translatable("options.reset");
    public static final BiFunction<Component, Component, Component> TEMPLATE = (a, b) -> Component.translatable("options.template", a, b);
    public static final Component ON = Component.translatable("options.on");
    public static final Component OFF = Component.translatable("options.off");

    private ConfigTranslate() {
    }

    public static Component tooltip(final String translate) {
        return Component.translatable(translate + ".tooltip");
    }
}
