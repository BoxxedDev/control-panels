package moth.boxxed.panels.api.module.config.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.config.gui.widgets.ConfigFrameWidget;
import moth.boxxed.panels.network.packet.ConfigureModulePacket;
import moth.boxxed.panels.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

public class ModuleConfigScreen extends Screen {
    public static final ResourceLocation CONFIG_SHEET = Dashpanels.path("textures/gui/module_config/module_config.png");
    private static final int SCROLL_BAR_MIN_HEIGHT = 5;

    private final BlockPos pos;
    private final Module moduleToConfigure;

    private int scroll = 0;
    private final int maxScroll;

    private final List<ConfigButton<?, ?>> configButtons = new ArrayList<>();

    private ModuleConfigValue<?, ?> selectedValue;
    private ConfigFrameBuilder frameBuilder;

    private int left = 10;
    private int top;

    public ModuleConfigScreen(Module module, BlockPos pos) {
        super(Component.literal(module.getName()));
        this.pos = pos;
        this.moduleToConfigure = module;

        int maxScroll = 0;
        ModuleConfig config = module.getConfig();
        for (ModuleConfigValue<?, ?> configValue : config.getValues()) {
            ConfigButton<?, ?> button = new ConfigButton<>(configValue);
            this.addWidget(button);
            this.configButtons.add(button);
            if (this.configButtons.size() > 8) {
                maxScroll += 16;
            }
        }
        this.maxScroll = maxScroll;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int height = guiGraphics.guiHeight();

        top = (height-160)/2;

        guiGraphics.blit(CONFIG_SHEET, left, top, 0, 0, 96, 160, 256, 256);
        if (this.configButtons.size() > 8) {
            this.renderScrollbar(guiGraphics, this.left, this.top, partialTick);
        }
        if (this.selectedValue != null && this.frameBuilder != null) {
            for (int i = 0; i < this.configButtons.size(); i++) {
                ConfigButton<?, ?> button = this.configButtons.get(i);
                ModuleConfigValue<?, ?> configValue = button.value;
                if (this.selectedValue == configValue) {
                    int y = Math.clamp(top+16+i*16 - scroll, top+16, top+128);
                    guiGraphics.blit(CONFIG_SHEET, left+91, y, 96, 32, 16, 16, 256, 256);
                    renderFieldsWindow(guiGraphics, left+100, y, mouseX, mouseY, partialTick);
                    break;
                }
            }
        }

        renderTitle(guiGraphics, left, top);
//        GuiUtil.blitNineSlice(guiGraphics, TOP_BOTTOM_BAR,
//                left+100, top, 100, 100,
//                16, 16, 96, 48,
//                256, 256,
//                6, 6, 6, 6);
        renderConfigValues(guiGraphics, left, top, mouseX, mouseY, partialTick);
    }

    public void renderTitle(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.drawScrollingString(this.font, Component.literal(this.moduleToConfigure.getName()), left+6, left+90, top+3, 0xEFFFFF);
    }

    public void renderConfigValues(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY, float partialTick) {
        guiGraphics.enableScissor(left, top+18, left+96,  top+142);

        for (int i = 0; i < this.configButtons.size(); i++) {
            ConfigButton<?, ?> button = this.configButtons.get(i);
            button.setX(left+7);
            button.setY(top+18 + i*16 - scroll);
            button.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.disableScissor();
    }

    public void renderFieldsWindow(GuiGraphics graphics, int left, int top, int mouseX, int mouseY, float partialTick) {
        int frameWidth = 16 + this.frameBuilder.getColumnsWidth() + ConfigFrameBuilder.PADDING;
        int frameHeight = 16 + this.frameBuilder.getRowsHeight() + ConfigFrameBuilder.PADDING;

        GuiUtil.blitNineSlice(
                graphics, CONFIG_SHEET,
                left, top, frameWidth, frameHeight,
                16, 16, 96, 48,
                256, 256,
                6, 6, 6, 6
        );

        for (int row = 0; row <= this.frameBuilder.getRows(); row++) {
            for (int column = 0; column < this.frameBuilder.getColumns(); column++) {
                ConfigFrameWidget<?, ? extends ModuleConfigValue<?, ?>> widget = this.frameBuilder.getWidgets().get(row, column);
                if (widget == null)
                    continue;

                int x = 8 + left;
                if (column >= 1)
                    for (int i = 0; i < column; i++) {
                        x += this.frameBuilder.getColumnWidth(i) + ConfigFrameBuilder.PADDING;
                    }

                int y = 8 + top;
                if (row >= 1)
                    for (int i = 0; i < row; i++) {
                        y += this.frameBuilder.getRowHeight(i) + ConfigFrameBuilder.PADDING;
                    }

                widget.setX(x);
                widget.setY(y);
            }
        }

        for (ConfigFrameWidget<?, ? extends ModuleConfigValue<?, ?>> widget : this.frameBuilder.getWidgets().values()) {
            widget.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }

    public void renderScrollbar(GuiGraphics graphics, int left, int top, float partialTick) {
        int height = (int) Math.floor(128*(8f/this.configButtons.size()));
        int heightDiff = 128-height;
        int yOffset = (int) (Mth.clampedMap(this.scroll, 0, this.maxScroll, 0, 1)*heightDiff);
        GuiUtil.blitHorizontalTriSlice(
                graphics, CONFIG_SHEET,
                left, top + 16 + yOffset, 2, Math.max(height, SCROLL_BAR_MIN_HEIGHT),
                2, 5, 112, 32,
                256, 256,
                2, 2
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private <T, R extends ModuleConfigValue<T, R>> void setSelectedValue(R value) {
        if (this.frameBuilder != null) {
            for (ConfigFrameWidget<?, ?> widget : this.frameBuilder.getWidgets().values()) {
                this.removeWidget(widget);
                widget.onRemove();
            }
        }

        this.selectedValue = value;

        this.frameBuilder = new ConfigFrameBuilder();
        this.selectedValue.buildGuiFrame(this.frameBuilder);

        for (ConfigFrameWidget<?, ? extends ModuleConfigValue<?, ?>> widget : this.frameBuilder.getWidgets().values()) {
            this.addWidget(widget);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        if (this.configButtons.size() > 8 &&
            mouseX >= left && mouseX <= left+96 &&
            mouseY >= top+16 && mouseY <= top+144) {
            this.scroll = (int) Math.clamp(this.scroll-scrollY*8, 0, this.maxScroll);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!(mouseY >= this.top+16 && mouseY <= this.top+144)) {
            return true;
        }

        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(guieventlistener);
                if (button == 0) {
                    this.setDragging(true);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void onClose() {
        if (Minecraft.getInstance().level == null) {
            super.onClose();
            return;
        }
        Map<String, CompoundTag> mapToSend = new HashMap<>();
        RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
        for (ConfigButton<?, ?> button : this.configButtons) {
            ModuleConfigValue<?, ?> value = button.value;

            CompoundTag tag = new CompoundTag();
            value.save(tag, registryAccess);
            mapToSend.put(value.getId(), tag);
        }
        PacketDistributor.sendToServer(
                new ConfigureModulePacket(
                        this.pos,
                        this.moduleToConfigure.getName(),
                        mapToSend
                )
        );
        super.onClose();
    }

    public class ConfigButton<T, R extends ModuleConfigValue<T, R>> extends AbstractWidget {
        private final boolean revertable;
        private final RevertableButton revertButton;
        private final R value;

        public ConfigButton(@UnknownNullability ModuleConfigValue<?, ?> value) {
            super(0, 0, value.isRevertable() ? 66 : 82, 12, value.getName());
            this.value = (R) value;

            this.revertable = value.isRevertable();
            if (this.revertable) {
                this.revertButton = new RevertableButton(0, 0, 12, 12);
                ModuleConfigScreen.this.addWidget(revertButton);
            } else {
                this.revertButton = null;
            }
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            if (this.revertButton != null)
                this.revertButton.setX(x+70);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            if (this.revertButton != null)
                this.revertButton.setY(y);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int vOffset = this.revertable ? 0 : 16;
            guiGraphics.blit(CONFIG_SHEET, this.getX(), this.getY(), 96,vOffset, this.width, this.height);
            boolean isSelected = ModuleConfigScreen.this.selectedValue == this.value;
            guiGraphics.drawScrollingString(ModuleConfigScreen.this.font, this.getMessage(), this.getX()+3, this.getX()+this.width-3-(isSelected ? 7 : 0), this.getY()+2, 0xEFFFFF);
            if (isSelected) {
                guiGraphics.blit(CONFIG_SHEET, this.getX()+this.getWidth()-7, this.getY(), 144, 32, 7, 12);
            }
            if (this.isMouseOver(mouseX, mouseY)) {
                RenderSystem.enableBlend();
                guiGraphics.setColor(1, 1, 1, 0.25f);
                guiGraphics.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 1, 0xFFFFFFFF);
                guiGraphics.setColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
            }

            if (this.revertButton != null) {
                this.revertButton.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            ModuleConfigScreen.this.setSelectedValue(this.value);
            super.onClick(mouseX, mouseY, button);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        public class RevertableButton extends AbstractWidget {
            public RevertableButton(int x, int y, int width, int height) {
                super(x, y, width, height, Component.empty());
            }

            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(CONFIG_SHEET, this.getX(), this.getY(), 166,0, this.width, this.height);
                if (this.isMouseOver(mouseX, mouseY)) {
                    RenderSystem.enableBlend();
                    guiGraphics.setColor(1, 1, 1, 0.25f);
                    guiGraphics.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 1, 0xFFFFFFFF);
                    guiGraphics.setColor(1, 1, 1, 1);
                    RenderSystem.disableBlend();
                }
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }

            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                if (!Objects.equals(ConfigButton.this.value.get(), ConfigButton.this.value.getDefault())) {
                    ConfigButton.this.value.set(ConfigButton.this.value.getDefault());
                }

                super.onClick(mouseX, mouseY, button);
            }
        }
    }
}
