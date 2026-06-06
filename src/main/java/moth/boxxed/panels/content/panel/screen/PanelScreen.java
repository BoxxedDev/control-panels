package moth.boxxed.panels.content.panel.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.network.packet.SavePanelModulesPacket;
import moth.boxxed.panels.network.packet.SetPlayerSlotPacket;
import moth.boxxed.panels.util.BasicButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: potentially like, bring some components of this into different widgets/renderables
public class PanelScreen extends AbstractContainerScreen<PanelMenu> {
    private static final ResourceLocation SPRITE = Dashpanels.path("textures/gui/container/panel.png");

    private static final ResourceLocation SAVE = Dashpanels.path("container/panel/save");
    private static final ResourceLocation SAVE_HOVERED = Dashpanels.path("container/panel/save_highlighted");
    private static final ResourceLocation EXIT = Dashpanels.path("container/panel/exit");
    private static final ResourceLocation EXIT_HOVERED = Dashpanels.path("container/panel/exit_highlighted");
    private static final ResourceLocation WRITE_NAME = Dashpanels.path("container/panel/write_name");
    private static final ResourceLocation WRITE_NAME_HOVERED = Dashpanels.path("container/panel/write_name_highlighted");
    private static final ResourceLocation MODULE_OUTLINE = Dashpanels.path("container/panel/module_outline");
    private static final ResourceLocation BACKDROP = Dashpanels.path("container/panel/backdrop");

    private ModuleMap modulesToSave;

    private Rect2i contentArea;

    private PseudoModule draggingModule;
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
                this.leftPos+24, this.topPos+37,
                128, 96
        );

        this.modulesToSave = new ModuleMap();
        this.modulesToSave.putAll(this.menu.holder.getModules());

        this.addRenderableWidget(
                new BasicButtonWidget(
                        WRITE_NAME, WRITE_NAME_HOVERED,
                        this.leftPos+123, this.topPos+9, 18, 18,
                        Component.translatable("widget.dashpanels.panel.write_name"),
                        this::writeName
                )
        );
        this.addRenderableWidget(
                new BasicButtonWidget(
                        SAVE, SAVE_HOVERED,
                        this.leftPos + 142, this.topPos + 9, 18, 18,
                        Component.translatable("widget.dashpanels.panel.save"),
                        this::onClose
                        )
        );
//        this.addRenderableWidget(
//                new BasicButtonWidget(
//                        EXIT, EXIT_HOVERED,
//                        this.leftPos + 133, this.topPos + 9, 18, 18,
//                        Component.translatable("widget.dashpanels.panel.exit"),
//                        () -> this.onClose(true)
//                )
//        );

        this.nameEditBox = new EditBox(
                this.font,
                this.leftPos+20, this.topPos+14,99, 12,
                Component.translatable("widget.dashpanels.panel.edit_box.module_name")
        );
        this.nameEditBox.setBordered(false);
        this.addRenderableWidget(this.nameEditBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
//        this.renderBackground(graphics, mouseX, mouseY, partialTick);
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
        RenderSystem.enableBlend();
        graphics.setColor(1f,1f,1f, Mth.map((float) Math.sin(backdropPulse), -1, 1, 0.05f, 0.2f));
        graphics.blitSprite(
                BACKDROP,
                this.contentArea.getX(),
                this.contentArea.getY(),
                this.contentArea.getWidth(),
                this.contentArea.getHeight()
        );
        graphics.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
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
//        guiGraphics.drawString(this.font, this.title, 22, 5, 0x35324e);
    }

    private void renderDraggingModule(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.draggingModule == null)
            return;

        int sizeX = Math.max(this.draggingModule.module.getSize().x*8, 8);
        int sizeY = Math.max(this.draggingModule.module.getSize().y*8, 8);

        int localPosX = Math.clamp((mouseX-(sizeX/2))-this.contentArea.getX(), 0, this.contentArea.getWidth());
        int localPosY = Math.clamp((mouseY-(sizeY/2))-this.contentArea.getY(), 0, this.contentArea.getHeight());
        localPosX = Math.clamp(Math.divideExact(localPosX, 8), 0, 16-this.draggingModule.module.getSize().x);
        localPosY = Math.clamp(Math.divideExact(localPosY, 8), 0, 12-this.draggingModule.module.getSize().y);

        int posX = Math.clamp(localPosX*8+this.contentArea.getX(),
                this.contentArea.getX(), this.contentArea.getX()+this.contentArea.getWidth()-sizeX);
        int posY = Math.clamp(localPosY*8+this.contentArea.getY(),
                this.contentArea.getY(), this.contentArea.getY()+this.contentArea.getHeight()-sizeY);
        if (this.draggingModuleIntersecting()){
            graphics.setColor(1,0.5f,0.5f,0.9f);
        }
        RenderSystem.enableBlend();
        graphics.blitSprite(MODULE_OUTLINE, posX, posY, sizeX, sizeY);
        graphics.renderItem(new ItemStack(this.draggingModule.module.type.associatedItem), posX+(sizeX/2)-8, posY+(sizeY/2)-8);
        RenderSystem.disableBlend();
        this.draggingModule.module.setPos(localPosX, localPosY);
        graphics.setColor(1f, 1, 1f, 1f);
    }

    private void renderModules(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Map.Entry<String, Module> entry : this.modulesToSave.entrySet()) {
            int sizeX = Math.max(entry.getValue().getSize().x*8, 8);
            int sizeY = Math.max(entry.getValue().getSize().y*8, 8);
            int posX = this.contentArea.getX()+entry.getValue().getPos().x*8;
            int posY = this.contentArea.getY()+entry.getValue().getPos().y*8;
            if (entry.getKey().equals(this.selectedModule))
                graphics.setColor(0.5f, 1, 0.5f, 0.9f);
            RenderSystem.enableBlend();
            graphics.blitSprite(MODULE_OUTLINE, posX, posY, sizeX, sizeY);
            graphics.renderItem(new ItemStack(entry.getValue().type.associatedItem), posX+(sizeX/2)-8, posY+(sizeY/2)-8);
            RenderSystem.disableBlend();
            graphics.setColor(1f, 1, 1f, 1f);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_E)
            return true;

        if (keyCode == 256 && this.draggingModule != null) {
            Module module = this.modulesToSave.remove(this.draggingModule.getName());
            addItemToInv(module);
            this.draggingModule = null;
            return true;
        }

        if (keyCode == InputConstants.KEY_DELETE && !this.selectedModule.isEmpty()) {
            Module module = this.modulesToSave.remove(this.selectedModule);
            addItemToInv(module);
            this.selectedModule = "";
            this.nameEditBox.setValue("");
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void addItemToInv(Module module) {
        if (!this.getMenu().inventory.player.isCreative()) {
            int slotExisting = this.getMenu().inventory.getSlotWithRemainingSpace(new ItemStack(module.type.associatedItem));
            if (slotExisting == -1) {
                slotExisting = this.getMenu().inventory.getFreeSlot();
            }
            ItemStack existingStack = this.getMenu().inventory.getItem(slotExisting);
            if (existingStack != ItemStack.EMPTY) {
                existingStack.setCount(existingStack.getCount() + 1);
                PacketDistributor.sendToServer(new SetPlayerSlotPacket(slotExisting, existingStack, existingStack.getCount()));
            } else {
                this.getMenu().inventory.setItem(slotExisting, new ItemStack(module.type.associatedItem));
            }
        }
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
                        Module module = entry.getValue().create(0, 0);
                        module.name = validateName(location+"_0");
                        this.draggingModule = new PseudoModule(module, slot);
                        return true;
                    }
                }
            }
        }

        if (this.draggingModule != null && this.contentArea.contains((int) mouseX, (int) mouseY)) {
            if (!this.draggingModuleIntersecting()) {
                this.modulesToSave.put(this.draggingModule.getName(), this.draggingModule.module);
                if (!this.getMenu().inventory.player.isCreative()) {
                    this.draggingModule.boundSlot.remove(1);
                    ItemStack slotStack = this.draggingModule.boundSlot.getItem();
                    PacketDistributor.sendToServer(new SetPlayerSlotPacket(
                            this.draggingModule.boundSlot.getSlotIndex(),
                            slotStack,
                            slotStack.getCount()
                    ));
                }
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
                this.draggingModule = new PseudoModule(entry.getValue(), null);
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
            if (entry.getValue().rect.intersects(this.draggingModule.module.rect))
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
        String nameToSet = validateName(this.nameEditBox.getValue());
        this.modulesToSave.rename(this.selectedModule, nameToSet);
        this.selectedModule = nameToSet;
        this.nameEditBox.setValue("");
    }

    private String validateName(String tryName) {
        String name = tryName;
        if (!(this.modulesToSave.containsKey(name) || this.getMenu().takenNames.contains(name)))
            return name;
        name = tryName+"_0";
        int i=0;
        while (this.modulesToSave.containsKey(name) || this.getMenu().takenNames.contains(name)) {
            i++;
            name = (tryName+"_%d").formatted(i);
        }
        return name;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(SPRITE, this.leftPos, this.topPos, 0, 0, this.imageWidth+25, this.imageHeight);
    }

    @Override
    public void onClose() {
        PanelBlockEntity be = PanelScreen.this.getMenu().holder;
        Map<String, Module.ModuleInfo> moduleInfoMap = new HashMap<>();
        for (Map.Entry<String, Module> entry : PanelScreen.this.modulesToSave) {
            moduleInfoMap.put(
                    entry.getKey(),
                    Module.ModuleInfo.fromModule(entry.getValue(), be.getLevel().registryAccess())
            );
        }
        PacketDistributor.sendToServer(new SavePanelModulesPacket(moduleInfoMap, be.getBlockPos()));
        super.onClose();
    }

    public static class PseudoModule {
        public Module module;
        public Slot boundSlot;

        public PseudoModule(Module module, Slot boundSlot) {
            this.module = module;
            this.boundSlot = boundSlot;
        }

        public String getName() {
            return this.module.name;
        }
    }
}
