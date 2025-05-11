package btw.lowercase.optiboxes.config;

import btw.lowercase.lightconfig.lib.ConfigTranslate;
import btw.lowercase.optiboxes.OptiBoxesClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class OptiBoxesConfigScreen extends Screen {
    public static final Component TITLE = Component.translatable("options.optiboxes.title");

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
        gridLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter().alignVerticallyMiddle();
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
        // Didn't iterate fields here because I wanted custom order
        rowHelper.addChild(config.enabled.createWidget());
        rowHelper.addChild(config.showOverworldForUnknownDimension.createWidget());
        rowHelper.addChild(config.processOptiFine.createWidget(() -> this.minecraft.reloadResourcePacks()));
        rowHelper.addChild(config.processMCPatcher.createWidget(() -> this.minecraft.reloadResourcePacks()));
        rowHelper.addChild(config.renderSunMoon.createWidget());
        rowHelper.addChild(config.renderStars.createWidget());
        layout.addToContents(gridLayout);

        GridLayout footerGridLayout = new GridLayout();
        footerGridLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper footerRowHelper = footerGridLayout.createRowHelper(2);
        footerRowHelper.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).width(125).build());
        footerRowHelper.addChild(Button.builder(ConfigTranslate.RESET, (button) -> {
            config.reset();
            this.minecraft.reloadResourcePacks();
        }).width(125).build());
        layout.addToFooter(footerGridLayout);

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            OptiBoxesClient.getConfig().save();
            this.minecraft.setScreen(parent);
        }
    }
}
