package btw.lowercase.optiboxes.config;

import btw.lowercase.optiboxes.OptiBoxesClient;
import btw.lowercase.lightconfig.lib.field.BooleanConfigField;
import btw.lowercase.lightconfig.lib.field.ConfigField;
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

public class OptiBoxesConfigScreen extends Screen {
    private static final String PREFIX = "options.optiboxes.";
    private static final Component TITLE = Component.translatable(PREFIX + "title");
    private static final BiFunction<Component, Component, Component> TEMPLATE = (a, b) -> Component.translatable(PREFIX + "template", a, b);
    private static final Component ON = Component.translatable(PREFIX + "on");
    private static final Component OFF = Component.translatable(PREFIX + "off");

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

        // Didn't iterate fields here cause I wanted custom order
        rowHelper.addChild(createConfigButton(config.enabled));
        rowHelper.addChild(createConfigButton(config.showOverworldForUnknownDimension));
        rowHelper.addChild(createConfigButton(config.processOptiFine, () -> this.minecraft.reloadResourcePacks()));
        rowHelper.addChild(createConfigButton(config.processMCPatcher, () -> this.minecraft.reloadResourcePacks()));
        rowHelper.addChild(createConfigButton(config.renderSunMoon));
        rowHelper.addChild(createConfigButton(config.renderStars));

        layout.addToContents(gridLayout);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).width(200).build());
        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
    }

    private Button createConfigButton(ConfigField<?> configField, Runnable onClick) {
        if (configField instanceof BooleanConfigField booleanConfigField) {
            final String translate = PREFIX + configField.getName();
            Component name = Component.translatable(translate);
            Button.Builder builder = Button.builder(TEMPLATE.apply(name, booleanConfigField.isEnabled() ? ON : OFF), (button) -> {
                booleanConfigField.toggle();
                onClick.run();
                button.setMessage(TEMPLATE.apply(name, booleanConfigField.isEnabled() ? ON : OFF));
            });
            builder.tooltip(Tooltip.create(tooltip(translate)));
            return builder.build();
        } else {
            throw new RuntimeException("TODO: Support other config field types when creating a button.");
        }
    }

    private Button createConfigButton(ConfigField<?> configField) {
        return this.createConfigButton(configField, () -> {
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, tickDelta);
        super.render(guiGraphics, mouseX, mouseY, tickDelta);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            OptiBoxesClient.getConfig().save();
            this.minecraft.setScreen(parent);
        }
    }
}
