package moth.boxxed.panels.compat.create.panel_link.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.io.IOEntry;
import moth.boxxed.panels.api.module.io.ModuleIOType;
import moth.boxxed.panels.compat.create.panel_link.ModuleLinkEntries;
import moth.boxxed.panels.network.packet.PanelLinkSaveEntriesPacket;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PanelLinkScreen extends AbstractSimiContainerScreen<PanelLinkMenu> {
    public static final ResourceLocation MAIN = Dashpanels.path("textures/gui/container/control_link.png");

    private IconButton confirm;
    private IconButton add;

    public EntryEditorScreen editorScreen;

    private final ModuleLinkEntries entriesToAdd;
    private List<ModuleEntryWidget> entryWidgets;

    private float scrollOffset = 0;
    private float scrollLerp = 0;
    private int contentHeight = 0;

    public PanelLinkScreen(PanelLinkMenu container, Inventory inv, Component title) {
        super(container, inv, title);

        this.entriesToAdd = new ModuleLinkEntries();
        this.entriesToAdd.addAll(container.contentHolder.getModuleEntries().getMap());

        this.editorScreen = new EntryEditorScreen(this);
    }

    @Override
    protected void init() {
        setWindowSize(220, 158);
        super.init();

        this.editorScreen.init();

        this.confirm = new IconButton(this.leftPos + 195, this.topPos + 134, AllIcons.I_CONFIRM)
                .withCallback(this::onClose);
        this.add = new IconButton(this.leftPos + 166, this.topPos + 134, AllIcons.I_ADD)
                .withCallback(() -> this.modifyEntry(null));

        this.entryWidgets = new ArrayList<>();

        addAllWidgets();

        rebuildEntryWidgets();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    public void addAllWidgets() {
        this.addWidget(this.confirm);
        this.addWidget(this.add);

        for (ModuleEntryWidget widget : this.entryWidgets) {
            widget.enableInnerWidgets();
        }
    }

    public void removeAllWidgets() {
        this.removeWidget(this.confirm);
        this.removeWidget(this.add);

        for (ModuleEntryWidget widget : this.entryWidgets) {
            widget.disableInnerWidgets();
        }
    }

    @Override
    public <T extends GuiEventListener & NarratableEntry> T addWidget(T listener) {
        return super.addWidget(listener);
    }

    @Override
    public void removeWidget(GuiEventListener listener) {
        super.removeWidget(listener);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.translate(0,0,-1);

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.enableScissor(0, this.topPos + 16, this.width, this.topPos + 127);
        for (ModuleEntryWidget widget : this.entryWidgets) {
            widget.render(graphics, mouseX, mouseY, partialTick, stack);
        }
        graphics.disableScissor();

        if (this.editorScreen.active) {
            this.editorScreen.render(graphics, mouseX, mouseY, partialTick);
        }

        stack.popPose();

        this.scrollLerp = Math.lerp(this.scrollLerp, this.scrollOffset, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int titleX = (this.imageWidth / 2) - (this.font.width(this.getTitle())/2);

        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        guiGraphics.blit(MAIN, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.drawString(this.font, this.getTitle(), this.leftPos+titleX, this.topPos+4, 5841956, false);

        if (!this.menu.showSlots) {
            this.confirm.render(guiGraphics, mouseX, mouseY, partialTick);
            this.add.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.enableScissor(0, this.topPos + 16, this.width, this.topPos + 127);
        for (ModuleEntryWidget widget : this.entryWidgets) {
            widget.renderBG(guiGraphics, partialTick, mouseX, mouseY);
        }
        guiGraphics.disableScissor();
        stack.popPose();

        if (this.editorScreen.active) {
            this.editorScreen.renderBG(guiGraphics);
        }
    }

    public ModuleLinkEntries getEntriesToAdd() {
        return this.entriesToAdd;
    }

    private void modifyEntry(ModuleEntryWidget widget) {
        this.removeAllWidgets();
        this.editorScreen.startModification(widget == null ? null : widget.entry, newEntry -> {
            if (newEntry != null) {
                ModuleEntryWidget current = null;
                for (ModuleEntryWidget entry : this.entryWidgets) {
                    if (Objects.equals(entry.entry.getEntry(), newEntry.getEntry())) {
                        current = entry;
                        break;
                    }
                }

                if (current != null) {
                    this.entriesToAdd.set(newEntry.getEntry(), newEntry);
                    current.entry = newEntry;
                } else {
                    this.addEntry(newEntry);
                }
            }
            this.addAllWidgets();
        });
    }

    private void addEntry(ModuleLinkEntries.ModuleEntry newEntry) {
        this.entriesToAdd.set(newEntry.getEntry(), newEntry);
        rebuildEntryWidgets();
    }

    private void removeEntry(ModuleEntryWidget moduleEntryWidget) {
        this.entriesToAdd.set(moduleEntryWidget.entry.getEntry(), null);
        rebuildEntryWidgets();
    }

    public void rebuildEntryWidgets() {
        for (ModuleEntryWidget widget : this.entryWidgets) {
            widget.disableInnerWidgets();
        }

        this.entryWidgets.clear();
        for (Map.Entry<IOEntry, ModuleLinkEntries.ModuleEntry> entry : this.entriesToAdd.getMap().entrySet()) {
            ModuleEntryWidget widget = new ModuleEntryWidget(entry.getValue());
            widget.enableInnerWidgets();
            this.entryWidgets.add(widget);
        }

        this.contentHeight = 20 + (this.entryWidgets.size()*35);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.editorScreen.active) {
            this.scrollOffset = (float) Math.max(Math.min(this.scrollOffset - scrollY*25f, this.contentHeight-116f), 0f);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new PanelLinkSaveEntriesPacket(this.entriesToAdd, this.getMenu().contentHolder.getBlockPos()));
        super.onClose();
    }

    private class ModuleEntryWidget {
        private final IconButton edit;
        private final IconButton delete;
        private ModuleLinkEntries.ModuleEntry entry;
        public boolean active = true;

        public ModuleEntryWidget(ModuleLinkEntries.ModuleEntry entry) {
            this.edit = new CustomAdjustableButton(AllIcons.I_CONFIG_SAVE)
                    .withCallback(() -> PanelLinkScreen.this.modifyEntry(this));
            this.delete = new CustomAdjustableButton(AllIcons.I_TRASH)
                    .withCallback(() -> PanelLinkScreen.this.removeEntry(this));

            this.entry = entry;
        }

        private float getCurrentHeight(float partialTick) {
            int index = PanelLinkScreen.this.entryWidgets.indexOf(this);

            return (PanelLinkScreen.this.topPos + 26) + (index*35) - PanelLinkScreen.this.scrollLerp;
        }

        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, PoseStack stack) {
            stack.pushPose();

            int x = PanelLinkScreen.this.leftPos + 13;
            float y = this.getCurrentHeight(partialTick);
            stack.translate(x, y, 0);

            this.edit.setPosition(x+147, (int) (y+6));
            this.delete.setPosition(x+170, (int) (y+6));

            boolean isInContent = y+16 < PanelLinkScreen.this.topPos+127 && y+30 > PanelLinkScreen.this.topPos+16;
            this.edit.setActive(isInContent && this.active);
            this.delete.setActive(isInContent && this.active);

            this.renderFrequencies(graphics, stack);

            stack.popPose();
        }

        public void renderBG(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            PoseStack stack = graphics.pose();

            stack.pushPose();

            int x = PanelLinkScreen.this.leftPos + 13;
            float y = this.getCurrentHeight(partialTick);
            stack.translate(x, y, 0);

            graphics.blit(MAIN, 0, 0, 0, 158, 194, 30);

            Component text = Component.literal(this.entry.getEntry().toString());
            int maxX = 94;
            int minX = 25;
            int maxWidth = maxX - minX;
            int textWidth = font.width(text.getVisualOrderText());
            int centerX = (minX + maxX) / 2;
            int minY = 10;
            int maxY = minY+font.lineHeight;
            if (textWidth <= maxWidth) {
                graphics.drawString(font, text, minX, minY, 0xFFFFFF);
            } else {
                int i = font.width(text);
                int j = (minY + maxY - 9) / 2 + 1;
                int k = maxX - minX;
                if (i > k) {
                    int l = i - k;
                    double d0 = (double) Util.getMillis() / 1000.0;
                    double d1 = java.lang.Math.max((double)l * 0.5, 3.0);
                    double d2 = java.lang.Math.sin((java.lang.Math.PI / 2) * java.lang.Math.cos((java.lang.Math.PI * 2) * d0 / d1)) / 2.0 + 0.5;
                    double d3 = Mth.lerp(d2, 0.0, (double)l);
                    graphics.enableScissor(x+minX, 0, x+maxX, graphics.guiHeight());
                    graphics.drawString(font, text, minX - (int)d3, j, 0xFFFFFF);
                    graphics.disableScissor();
                } else {
                    int i1 = Mth.clamp(centerX, minX + i / 2, maxX - i / 2);
                    graphics.drawCenteredString(font, text, i1, j, 0xFFFFFF);
                }
            }

            Font font = Minecraft.getInstance().font;
            Component type = this.entry.getEntry().type() == ModuleIOType.INPUT ^ this.entry.getEntry().type() == ModuleIOType.MULTI_INPUT ?
                    Component.literal("O").withStyle(ChatFormatting.RED) :
                    Component.literal("I").withStyle(ChatFormatting.GREEN);
            graphics.drawCenteredString(font, type, 12, (minY + maxY - 9) / 2 + 1, 0xFFFFFF);

            stack.pushPose();
            stack.translate(147, 6, 0);
            this.edit.render(graphics, mouseX, mouseY, partialTick);
            stack.translate(23, 0, 0);
            this.delete.render(graphics, mouseX, mouseY, partialTick);
            stack.popPose();

            stack.popPose();
        }

        private void renderFrequencies(GuiGraphics graphics, PoseStack stack) {
            if (!PanelLinkScreen.this.editorScreen.active) {
                stack.pushPose();
                stack.translate(102, 7, 0);
                GuiGameElement.of(this.entry.firstItemStack()).render(graphics);
                stack.translate(23,0,0);
                GuiGameElement.of(this.entry.secondItemStack()).render(graphics);
                stack.popPose();
            }
        }

        public void enableInnerWidgets() {
            PanelLinkScreen.this.addWidget(this.edit);
            PanelLinkScreen.this.addWidget(this.delete);
            this.edit.setActive(true);
            this.delete.setActive(true);
            this.active = true;
        }

        public void disableInnerWidgets() {
            PanelLinkScreen.this.removeWidget(this.edit);
            PanelLinkScreen.this.removeWidget(this.delete);
            this.edit.setActive(false);
            this.delete.setActive(false);
            this.active = false;
        }
    }

    public static class CustomAdjustableButton extends IconButton {
        public CustomAdjustableButton(ScreenElement icon) {
            super(0, 0, icon);
        }

        @Override
        public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

                final AllGuiTextures button = !this.active ? AllGuiTextures.BUTTON_DISABLED
                        : this.isHovered && AllKeys.isMouseButtonDown(0) ? AllGuiTextures.BUTTON_DOWN
                        : this.isHovered ? AllGuiTextures.BUTTON_HOVER
                        : this.green ? AllGuiTextures.BUTTON_GREEN : AllGuiTextures.BUTTON;

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                graphics.blit(button.location, 0, 0, button.getStartX(), button.getStartY(), button.getWidth(),
                        button.getHeight());
                this.icon.render(graphics, 1, 1);
            }
        }
    }
}
