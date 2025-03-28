package btw.lowercase.optiboxes.config;

import btw.lowercase.optiboxes.OptiBoxesClient;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OptiBoxesConfig {
    private static final ConfigClassHandler<OptiBoxesConfig> CONFIG =
            ConfigClassHandler.createBuilder(OptiBoxesConfig.class).serializer(config ->
                    GsonConfigSerializerBuilder.create(config)
                            .setPath(YACLPlatform.getConfigDir().resolve(OptiBoxesClient.MOD_ID + ".json"))
                            .build()).build();

    @SerialEntry
    public boolean enabled = true;

    @SerialEntry
    public boolean processOptiFine = true;

    @SerialEntry
    public boolean processMCPatcher = false;

    @SerialEntry
    public boolean renderSunMoon = true;

    @SerialEntry
    public boolean renderStars = true;

    @SerialEntry
    public boolean showOverworldForUnknownDimension = true;

    public static Screen getConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(CONFIG, (defaults, config, builder) -> {
            builder.title(Component.translatable("options.optiboxes.title"));

            ConfigCategory.Builder category = ConfigCategory.createBuilder();
            category.name(Component.translatable("options.optiboxes.title"));
            Minecraft minecraft = Minecraft.getInstance();
            category.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("options.optiboxes.enabled"))
                    .description(OptionDescription.of(Component.translatable("options.optiboxes.enabled.tooltip")))
                    .binding(defaults.enabled, () -> config.enabled, (newVal) -> {
                        config.enabled = newVal;
                        minecraft.reloadResourcePacks();
                    })
                    .controller(TickBoxControllerBuilder::create)
                    .build());
            category.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("options.optiboxes.process_optifine"))
                    .description(OptionDescription.of(Component.translatable("options.optiboxes.process_optifine.tooltip")))
                    .binding(defaults.processOptiFine, () -> config.processOptiFine, (newVal) -> {
                        config.processOptiFine = newVal;
                        minecraft.reloadResourcePacks();
                    })
                    .controller(TickBoxControllerBuilder::create)
                    .build());
            category.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("options.optiboxes.process_mcpatcher"))
                    .description(OptionDescription.of(Component.translatable("options.optiboxes.process_mcpatcher.tooltip")))
                    .binding(defaults.processMCPatcher, () -> config.processMCPatcher, (newVal) -> {
                        config.processMCPatcher = newVal;
                        minecraft.reloadResourcePacks();
                    })
                    .controller(TickBoxControllerBuilder::create)
                    .build());
            category.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("options.optiboxes.render_sun_moon"))
                    .description(OptionDescription.of(Component.translatable("options.optiboxes.render_sun_moon.tooltip")))
                    .binding(defaults.renderSunMoon, () -> config.renderSunMoon, (newVal) -> config.renderSunMoon = newVal)
                    .controller(TickBoxControllerBuilder::create)
                    .build());
            category.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("options.optiboxes.render_stars"))
                    .description(OptionDescription.of(Component.translatable("options.optiboxes.render_stars.tooltip")))
                    .binding(defaults.renderStars, () -> config.renderStars, (newVal) -> config.renderStars = newVal)
                    .controller(TickBoxControllerBuilder::create)
                    .build());
            category.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("options.optiboxes.show_overworld_for_unknown_dimension"))
                    .description(OptionDescription.of(Component.translatable("options.optiboxes.show_overworld_for_unknown_dimension.tooltip")))
                    .binding(defaults.renderStars, () -> config.showOverworldForUnknownDimension, (newVal) -> config.showOverworldForUnknownDimension = newVal)
                    .controller(TickBoxControllerBuilder::create)
                    .build());
            builder.category(category.build());

            return builder;
        }).generateScreen(parent);
    }

    public static void load() {
        CONFIG.load();
    }

    public static OptiBoxesConfig instance() {
        return CONFIG.instance();
    }
}
