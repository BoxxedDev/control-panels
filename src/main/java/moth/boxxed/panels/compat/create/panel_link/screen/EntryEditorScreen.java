package moth.boxxed.panels.compat.create.panel_link.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import moth.boxxed.panels.api.module.io.IOEntry;
import moth.boxxed.panels.api.module.io.ModuleIOInfo;
import moth.boxxed.panels.api.module.io.ModuleIOType;
import moth.boxxed.panels.compat.create.panel_link.ModuleLinkEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class EntryEditorScreen {
    private final PanelLinkScreen parent;

    public IconButton confirm;
    public IconButton cancel;
    public IOEntryWidget moduleSelect;

    private int leftPos;
    private int topPos;

    public boolean active = false;

    private PseudoEntry pseudoEntry;
    private Consumer<ModuleLinkEntries.ModuleEntry> entryConsumer;

    public EntryEditorScreen(PanelLinkScreen parent) {
        this.parent = parent;
    }

    public void init() {
        this.leftPos = this.parent.width/2 - 103;
        this.topPos = this.parent.height/2 - 84;

        this.confirm = new IconButton(this.leftPos + 153, this.topPos + 25, AllIcons.I_CONFIRM)
                .withCallback(() -> {
                    if (this.pseudoEntry != null)
                        this.pseudoEntry.finish();
                });
        this.cancel = new IconButton(this.leftPos + 176, this.topPos + 25, AllIcons.I_TRASH)
                .withCallback(this::finishEmpty);

        List<IOEntry> entriesToShow = new ArrayList<>();
        List<ModuleIOInfo> infos = new ArrayList<>(this.parent.getMenu().modulesInfo);
        infos.sort(Comparator.comparing(ModuleIOInfo::name));
        for (ModuleIOInfo info : infos) {
            if (info == null) continue;
            List<IOEntry> ioEntries = new ArrayList<>(info.ioEntries());
            ioEntries.sort(Comparator.comparing(a -> a.extension().orElse("")));
            entriesToShow.addAll(ioEntries);
        }

        this.moduleSelect = new IOEntryWidget(
                this.leftPos + 29, this.topPos + 25, 73, 26,
                entriesToShow,
                Component.translatable("widget.dashpanels.panel_link.module_select")
        );
    }

    public void enable() {
        this.active = true;
    }

    public void disable() {
        this.parent.getMenu().showSlots = false;
        this.active = false;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        PoseStack stack = graphics.pose();

        stack.pushPose();
        stack.translate(0, 0, 2);
        this.confirm.render(graphics, mouseX, mouseY, partialTick);
        this.cancel.render(graphics, mouseX, mouseY, partialTick);

        this.moduleSelect.render(graphics, mouseX, mouseY, partialTick);
        if (this.moduleSelect.getCurrent() != null) {
            Font font = Minecraft.getInstance().font;
            Component type = this.moduleSelect.getCurrent().type() == ModuleIOType.INPUT ^ this.moduleSelect.getCurrent().type() == ModuleIOType.MULTI_INPUT ?
                    Component.literal("O").withStyle(ChatFormatting.RED) :
                    Component.literal("I").withStyle(ChatFormatting.GREEN);
            int y = topPos+39-(font.lineHeight);
            graphics.drawCenteredString(font, type, leftPos+18, y, 0xFFFFFF);
        }

        stack.popPose();
    }

    public void renderBG(GuiGraphics graphics) {
        PoseStack stack = graphics.pose();

        stack.pushPose();
        graphics.fillGradient(0, 0, this.parent.width, this.parent.height, 1, -1072689136, -804253680);

        stack.translate(0, 0, 2);
        graphics.blit(PanelLinkScreen.MAIN, leftPos, topPos, 0, 188, 206, 68);
        this.parent.renderPlayerInventory(graphics, leftPos+15, topPos+60);
        stack.popPose();
    }

    private void finishEmpty() {
        this.entryConsumer.accept(null);
        this.disable();
    }

    public PseudoEntry startModification(ModuleLinkEntries.ModuleEntry entryToModify, Consumer<ModuleLinkEntries.ModuleEntry> consumer) {
        PseudoEntry pseudoEntry = new PseudoEntry();
        if (entryToModify != null) {
            pseudoEntry.change(entryToModify.getEntry())
                    .first(entryToModify.first())
                    .second(entryToModify.second());

            this.parent.getEntriesToAdd().getMap().remove(entryToModify.getEntry());
        } else {
            pseudoEntry.first = RedstoneLinkNetworkHandler.Frequency.EMPTY;
            pseudoEntry.second = RedstoneLinkNetworkHandler.Frequency.EMPTY;
        }
        this.pseudoEntry = pseudoEntry;

        this.parent.addWidget(this.confirm);
        this.parent.addWidget(this.cancel);
        this.parent.addWidget(this.moduleSelect);
        this.moduleSelect.setCurrentEntry(pseudoEntry.moduleEntry);

        this.entryConsumer = consumer;

        PanelLinkMenu menu = this.parent.getMenu();
        menu.showSlots = true;
        this.active = true;
        menu.ghostInventory.setStackInSlot(0, pseudoEntry.first.getStack());
        menu.ghostInventory.setStackInSlot(1, pseudoEntry.second.getStack());

        return pseudoEntry;
    }

    public class PseudoEntry {
        public RedstoneLinkNetworkHandler.Frequency first;
        public RedstoneLinkNetworkHandler.Frequency second;

        public IOEntry moduleEntry;

        public PseudoEntry first(RedstoneLinkNetworkHandler.Frequency first) {
            this.first = first;
            return this;
        }

        public PseudoEntry second(RedstoneLinkNetworkHandler.Frequency second) {
            this.second = second;
            return this;
        }

        public PseudoEntry change(IOEntry entry) {
            this.moduleEntry = entry;
            return this;
        }

        public void finish() {
            this.change(EntryEditorScreen.this.moduleSelect.getCurrent());
            if (this.moduleEntry != null) {
                this.first(
                        RedstoneLinkNetworkHandler.Frequency.of(
                                EntryEditorScreen.this.parent.getMenu().ghostInventory.getStackInSlot(0)
                        )
                );
                this.second(
                        RedstoneLinkNetworkHandler.Frequency.of(
                                EntryEditorScreen.this.parent.getMenu().ghostInventory.getStackInSlot(1)
                        )
                );

                ModuleLinkEntries.ModuleEntry entry = new ModuleLinkEntries.ModuleEntry(
                        this.first,
                        this.second,
                        EntryEditorScreen.this.moduleSelect.getCurrent(),
                        EntryEditorScreen.this.parent.getMenu().contentHolder.getBlockPos()
                );
                EntryEditorScreen.this.entryConsumer.accept(entry);
            } else {
                EntryEditorScreen.this.finishEmpty();
            }

            EntryEditorScreen.this.parent.removeWidget(EntryEditorScreen.this.confirm);
            EntryEditorScreen.this.parent.removeWidget(EntryEditorScreen.this.cancel);
            EntryEditorScreen.this.parent.removeWidget(EntryEditorScreen.this.moduleSelect);

            EntryEditorScreen.this.disable();
        }
    }
}
