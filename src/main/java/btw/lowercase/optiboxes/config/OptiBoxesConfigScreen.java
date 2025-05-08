package btw.lowercase.optiboxes.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

public class OptiBoxesConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("options.optiboxes.title");
    private static final BiFunction<Component, Component, Component> TEMPLATE = (a, b) -> Component.translatable("options.optiboxes.template", a, b);
    private static final Component ON = Component.translatable("options.optiboxes.on");
    private static final Component OFF = Component.translatable("options.optiboxes.off");

    private static Component tooltip(String translate) {
        return Component.translatable(translate + ".tooltip");
    }

    private final Screen parent;
    private final OptiBoxesConfig config;

    public OptiBoxesConfigScreen(Screen parent, OptiBoxesConfig config) {
        super(TITLE);
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        assert this.minecraft != null;

        HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 61, 33);
        LinearLayout linearLayout = layout.addToHeader(LinearLayout.vertical().spacing(8));
        linearLayout.addChild(new StringWidget(TITLE, this.font), LayoutSettings::alignHorizontallyCenter);

        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);

        rowHelper.addChild(createConfigButton(
                "options.optiboxes.enabled",
                () -> config.enabled = !config.enabled,
                () -> config.enabled
        ));

        rowHelper.addChild(createConfigButton(
                "options.optiboxes.showOverworldForUnknownDimension",
                () -> config.showOverworldForUnknownDimension = !config.showOverworldForUnknownDimension,
                () -> config.showOverworldForUnknownDimension
        ));

        rowHelper.addChild(createConfigButton(
                "options.optiboxes.processOptiFine",
                () -> {
                    config.processOptiFine = !config.processOptiFine;
                    this.minecraft.reloadResourcePacks();
                },
                () -> config.processOptiFine
        ));

        rowHelper.addChild(createConfigButton(
                "options.optiboxes.processMCPatcher",
                () -> {
                    config.processMCPatcher = !config.processMCPatcher;
                    this.minecraft.reloadResourcePacks();
                },
                () -> config.processMCPatcher
        ));

        rowHelper.addChild(createConfigButton(
                "options.optiboxes.renderSunMoon",
                () -> config.renderSunMoon = !config.renderSunMoon,
                () -> config.renderSunMoon
        ));

        rowHelper.addChild(createConfigButton(
                "options.optiboxes.renderStars",
                () -> config.renderStars = !config.renderStars,
                () -> config.renderStars
        ));

        layout.addToContents(gridLayout);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).width(200).build());
        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
    }

    private Button createConfigButton(String translate, Runnable toggle, BooleanSupplier value) {
        Component name = Component.translatable(translate);
        Button.Builder builder = Button.builder(TEMPLATE.apply(name, value.getAsBoolean() ? ON : OFF), (button) -> {
            toggle.run();
            button.setMessage(TEMPLATE.apply(name, value.getAsBoolean() ? ON : OFF));
        });
        builder.tooltip(Tooltip.create(tooltip(translate)));
        return builder.build();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            OptiBoxesConfig.save();
            this.minecraft.setScreen(parent);
        }
    }
}
