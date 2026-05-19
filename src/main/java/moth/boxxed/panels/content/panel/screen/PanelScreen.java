package moth.boxxed.panels.content.panel.screen;

import com.mojang.blaze3d.platform.InputConstants;
import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.network.SavePanelModulesPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.HashMap;
import java.util.Map;

//TODO: potentially like, bring some components of this into different widgets/renderables
public class PanelScreen extends AbstractContainerScreen<PanelMenu> {
    private static final ResourceLocation SPRITE = ControlPanels.path("textures/gui/container/panel.png");

    private static final ResourceLocation SAVE = ControlPanels.path("container/panel/save");
    private static final ResourceLocation SAVE_HOVERED = ControlPanels.path("container/panel/save_highlighted");
    private static final ResourceLocation EXIT = ControlPanels.path("container/panel/exit");
    private static final ResourceLocation EXIT_HOVERED = ControlPanels.path("container/panel/exit_highlighted");
    private static final ResourceLocation WRITE_NAME = ControlPanels.path("container/panel/write_name");
    private static final ResourceLocation WRITE_NAME_HOVERED = ControlPanels.path("container/panel/write_name_highlighted");
    private static final ResourceLocation MODULE_OUTLINE = ControlPanels.path("container/panel/module_outline");

    private Map<String, Module> modulesToSave;
    private Rect2i contentArea;

    private Pair<String, Module> draggingModule;
    private String selectedModule = "";

    private EditBox nameEditBox;

    public PanelScreen(PanelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageHeight = 224;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.contentArea = new Rect2i(
                this.leftPos+24, this.topPos+32,
                128, 96
        );

        this.modulesToSave = new HashMap<>();
        this.modulesToSave.putAll(this.menu.holder.getModules());

        this.addRenderableWidget(
                new BasicButton(
                        SAVE, SAVE_HOVERED,
                        this.leftPos + 176, this.topPos + 141, 18, 18,
                        Component.translatable("widget.panels.panel.save"),
                        () -> this.onClose(false)
                        )
        );
        this.addRenderableWidget(
                new BasicButton(
                        EXIT, EXIT_HOVERED,
                        this.leftPos + 176, this.topPos + 163, 18, 18,
                        Component.translatable("widget.panels.panel.exit"),
                        () -> this.onClose(true)
                )
        );
        this.addRenderableWidget(
                new BasicButton(
                        WRITE_NAME, WRITE_NAME_HOVERED,
                        this.leftPos+105, this.topPos+13, 16, 16,
                        Component.translatable("widget.panels.panel.write_name"),
                        this::writeName
                )
        );

        this.nameEditBox = new EditBox(this.font, this.leftPos+22, this.topPos+15,80, 12, Component.translatable("widget.panels.panel.edit_box.module_name"));
        this.addRenderableWidget(this.nameEditBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderBackdrop(graphics);
        this.renderDraggingModule(graphics, mouseX, mouseY);
        this.renderModules(graphics, mouseX, mouseY);
        this.handleEditBox();
    }

    private float backdropPulse = 0;
    private void renderBackdrop(GuiGraphics graphics) {
        backdropPulse += 0.01f;
        if (this.draggingModule != null)
            graphics.fill(this.leftPos + 24, this.topPos + 32, this.leftPos + 152, this.topPos + 128, FastColor.ARGB32.color((int) (Math.abs(Math.sin(this.backdropPulse))*40), 0xFFFFFF));
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack itemstack, Slot slot, @Nullable String countString) {
        for (Map.Entry<ResourceKey<ModuleType<?>>, ModuleType<?>> entry : ModulesRegistry.MODULE_REGISTRY.entrySet()) {
            if (itemstack.getItem() == entry.getValue().associatedItem) {
                int x1 = slot.x;
                int y1 = slot.y;
                int x2 = slot.x+16;
                int y2 = slot.y+16;
                graphics.fill(x1, y1, x2, y2, 0x6F00FF00);
            }
        }
        super.renderSlotContents(graphics, itemstack, slot, countString);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 22, 5, 0x35324e);
    }

    private void renderDraggingModule(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.draggingModule == null)
            return;

        int sizeX = Math.max(this.draggingModule.getB().getSize().x*8, 8);
        int sizeY = Math.max(this.draggingModule.getB().getSize().y*8, 8);

        int localPosX = Math.clamp((mouseX-(sizeX/2))-this.contentArea.getX(), 0, this.contentArea.getWidth());
        int localPosY = Math.clamp((mouseY-(sizeY/2))-this.contentArea.getY(), 0, this.contentArea.getHeight());
        localPosX = Math.clamp(Math.divideExact(localPosX, 8), 0, 16-this.draggingModule.getB().getSize().x);
        localPosY = Math.clamp(Math.divideExact(localPosY, 8), 0, 12-this.draggingModule.getB().getSize().y);

        int posX = Math.clamp(localPosX*8+this.contentArea.getX(),
                this.contentArea.getX(), this.contentArea.getX()+this.contentArea.getWidth()-sizeX);
        int posY = Math.clamp(localPosY*8+this.contentArea.getY(),
                this.contentArea.getY(), this.contentArea.getY()+this.contentArea.getHeight()-sizeY);
        if (this.draggingModuleIntersecting()){
            graphics.setColor(1,0.5f,0.5f,1);
        }
        graphics.blitSprite(MODULE_OUTLINE, posX, posY, sizeX, sizeY);
        graphics.renderItem(new ItemStack((ItemLike) this.draggingModule.getB().type.associatedItem), posX+(sizeX/2)-8, posY+(sizeY/2)-8);
        graphics.setColor(1,1,1,1);
        this.draggingModule.getB().setPos(localPosX, localPosY);
    }

    private void renderModules(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Map.Entry<String, Module> entry : this.modulesToSave.entrySet()) {
            int sizeX = Math.max(entry.getValue().getSize().x*8, 8);
            int sizeY = Math.max(entry.getValue().getSize().y*8, 8);
            int posX = this.contentArea.getX()+entry.getValue().getPos().x*8;
            int posY = this.contentArea.getY()+entry.getValue().getPos().y*8;
            if (entry.getKey().equals(this.selectedModule))
                graphics.setColor(0.5f, 1, 0.5f, 1f);
            graphics.blitSprite(MODULE_OUTLINE, posX, posY, sizeX, sizeY);
            graphics.renderItem(new ItemStack((ItemLike) entry.getValue().type.associatedItem), posX+(sizeX/2)-8, posY+(sizeY/2)-8);
            graphics.setColor(1,1,1,1);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_E)
            return true;

        if (keyCode == 256 && this.draggingModule != null) {
            this.modulesToSave.remove(this.draggingModule.getA());
            this.draggingModule = null;
            return true;
        }

        if (keyCode == InputConstants.KEY_DELETE && !this.selectedModule.isEmpty()) {
            this.modulesToSave.remove(this.selectedModule);
            this.selectedModule = "";
            this.nameEditBox.setValue("");
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (containerEventHandlerMouseClicked(mouseX, mouseY, button))
            return true;

        Slot slot = this.findSlot(mouseX, mouseY);
        if (slot != null && this.draggingModule == null) {
            if (!slot.getItem().isEmpty()) {
                for (Map.Entry<ResourceKey<ModuleType<?>>, ModuleType<?>> entry : ModulesRegistry.MODULE_REGISTRY.entrySet()) {
                    if (slot.getItem().getItem() == entry.getValue().associatedItem) {
                        String location = entry.getKey().location().getPath();
                        String name = location+"_0";
                        int i=0;
                        while (this.modulesToSave.containsKey(name)) {
                            i++;
                            name = (location+"_%d").formatted(i);
                        }
                        this.draggingModule = new Pair<>(name, entry.getValue().create(0, 0));
                        return true;
                    }
                }
            }
        }

        if (this.draggingModule != null && this.contentArea.contains((int) mouseX, (int) mouseY)) {
            if (!this.draggingModuleIntersecting()) {
                this.modulesToSave.put(this.draggingModule.getA(), this.draggingModule.getB());
                this.draggingModule = null;
                return true;
            }
        }

        //Handle left click on module
        if (this.draggingModule == null && this.contentArea.contains((int) mouseX, (int) mouseY) && button == 0) {
            Map.Entry<String, Module> entry = this.findModule(mouseX, mouseY);
            if (entry != null) {
                if (!this.selectedModule.isEmpty()) {
                    this.selectedModule = "";
                    this.nameEditBox.setValue("");
                }
                this.draggingModule = new Pair<>(entry.getKey(), entry.getValue());
                this.modulesToSave.remove(entry.getKey());
                return true;
            }
        }

        //Handle right click on module
        if (this.draggingModule == null && this.contentArea.contains((int) mouseX, (int) mouseY) && button == 1) {
            Map.Entry<String, Module> entry = this.findModule(mouseX, mouseY);
            if (entry != null) {
                this.selectedModule = entry.getKey();
                this.nameEditBox.setValue(this.selectedModule);
                return true;
            }
        }

        this.nameEditBox.setValue("");
        this.selectedModule = "";

        return false;
    }

    private boolean draggingModuleIntersecting() {
        if (this.draggingModule == null) return false;
        for (Map.Entry<String, Module> entry : this.modulesToSave.entrySet())
            if (entry.getValue().rect.intersects(this.draggingModule.getB().rect))
                return true;
        return false;
    }

    private Map.Entry<String, Module> findModule(double mouseX, double mouseY) {
        for (Map.Entry<String, Module> entry : this.modulesToSave.entrySet()) {
            if (entry.getValue().rect.contains((mouseX-this.contentArea.getX())/8, (mouseY-this.contentArea.getY())/8)) {
                return entry;
            }
        }
        return null;
    }

    private boolean containerEventHandlerMouseClicked(double mouseX, double mouseY, int button) {
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

    public void handleEditBox() {
        this.nameEditBox.active = (!this.selectedModule.isEmpty());
    }

    private void writeName() {
        String toSet = this.nameEditBox.getValue();
        Module module = this.modulesToSave.remove(this.selectedModule);
        this.modulesToSave.put(toSet, module);
        this.selectedModule = toSet;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(SPRITE, this.leftPos, this.topPos, 0, 0, this.imageWidth+25, this.imageHeight);
    }

    public void onClose(boolean updateOnly) {
        PanelBlockEntity be = PanelScreen.this.getMenu().holder;
        Map<String, Module.ModuleInfo> moduleInfoMap = new HashMap<>();
        for (Map.Entry<String, Module> entry : PanelScreen.this.modulesToSave.entrySet()) {
            moduleInfoMap.put(
                    entry.getKey(),
                    Module.ModuleInfo.fromModule(entry.getValue())
            );
        }
        PacketDistributor.sendToServer(new SavePanelModulesPacket(moduleInfoMap, be.getBlockPos(), updateOnly));
        PanelScreen.this.minecraft.player.closeContainer();
    }

    public static class BasicButton extends AbstractButton {
        public ButtonFunction function;
        public Pair<ResourceLocation, ResourceLocation> spritePair;
        public BasicButton(ResourceLocation sprite, ResourceLocation hoverSprite, int x, int y, int width, int height, Component message, ButtonFunction function) {
            super(x, y, width, height, message);
            this.function = function;
            this.spritePair = new Pair<>(sprite, hoverSprite);
        }

        @Override
        public void onPress() {
            this.function.perform();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation location = this.spritePair.getA();
            if (this.isHovered())
                location = this.spritePair.getB();

            graphics.blitSprite(location, this.getX(), this.getY(), this.width, this.height);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        public interface ButtonFunction {
            void perform();
        }
    }
}
