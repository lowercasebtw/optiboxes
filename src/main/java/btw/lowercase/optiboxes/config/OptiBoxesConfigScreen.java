package btw.lowercase.optiboxes.config;

import btw.lowercase.optiboxes.OptiBoxesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OptiBoxesConfigScreen extends Screen {
    private final Screen parent;
    private final OptiBoxesConfig config;

    public OptiBoxesConfigScreen(Screen parent, OptiBoxesConfig config) {
        super(Component.translatable(getTranslationKey("title")));
        this.parent = parent;
        this.config = config;
    }

    private static String getTranslationKey(String optionKey) {
        return "options." + OptiBoxesClient.MOD_ID + "." + optionKey;
    }

    private static String getTooltipKey(String translationKey) {
        return translationKey + ".tooltip";
    }

    @Override
    protected void init() {
        addRenderableWidget(createBooleanOptionButton(this.width / 2 - 100 - 110, this.height / 2 - 10, 200, 20, "process_optifine", value -> config.processOptiFine = value, () -> config.processOptiFine, Minecraft.getInstance()::reloadResourcePacks));
        addRenderableWidget(createBooleanOptionButton(this.width / 2 - 100 + 110, this.height / 2 - 10, 200, 20, "process_mcpatcher", value -> config.processMCPatcher = value, () -> config.processMCPatcher, Minecraft.getInstance()::reloadResourcePacks));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).bounds(this.width / 2 - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        super.renderBackground(graphics, mouseX, mouseY, tickDelta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, tickDelta);
    }

    @Override
    public void onClose() {
        Objects.requireNonNull(this.minecraft).setScreen(parent);
    }

    @Override
    public void removed() {
        this.config.writeChanges();
    }

    private Button createBooleanOptionButton(int x, int y, int width, int height, String key, Consumer<Boolean> consumer, Supplier<Boolean> supplier, Runnable onChange) {
        String translationKey = getTranslationKey(key);
        Component text = Component.translatable(translationKey);
        Component tooltipText = Component.translatable(getTooltipKey(translationKey));
        return Button.builder(CommonComponents.optionStatus(text, supplier.get()), button -> {
            boolean newValue = !supplier.get();
            button.setMessage(CommonComponents.optionStatus(text, newValue));
            consumer.accept(newValue);
            onChange.run();
        }).bounds(x, y, width, height).tooltip(Tooltip.create(tooltipText)).build();
    }
}
