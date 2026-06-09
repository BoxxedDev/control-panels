package moth.boxxed.panels.content.cable.stripped.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.ModuleIOInfo;
import moth.boxxed.panels.api.module.ModuleIOType;
import moth.boxxed.panels.network.packet.ConfigureStrippedCablePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StrippedCableScreen extends AbstractContainerScreen<StrippedConfigMenu> {
    public static final ResourceLocation GUI = Dashpanels.path("textures/gui/container/stripped_cable_config.png");

    public List<String> list;

    private int centerX;
    private int centerY;

    public double scroll;
    private float textOffsetY;

    public StrippedCableScreen(StrippedConfigMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageWidth = 256;
        this.imageHeight = 48;

        this.centerX = this.width/2;
        this.centerY = this.height/2;

        this.list = new ArrayList<>();
        for (ModuleIOInfo info : this.menu.map.filterIOModules()) {
            if (info.type() == null) continue;
            if (info.type() == ModuleIOType.INPUT || info.type() == ModuleIOType.OUTPUT) {
                list.add(info.name());
            } else {
                String start = info.name();
                for (String extension : info.multiExtension()) {
                    list.add(start.concat(" - " + extension));
                }
            }
        }
        this.list.sort(null);
        String initConfig = this.menu.initialConfig;
        for (int i=0; i<this.list.size(); i++) {
            if (Objects.equals(this.list.get(i), initConfig)) {
                this.scroll = i;
                break;
            }
        }

        this.textOffsetY = centerY+Math.round(-this.scroll)*36;

        super.init();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        guiGraphics.blit(GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(GUI, this.leftPos, this.topPos + 8, 0, 64, this.imageWidth, 32);
        RenderSystem.disableBlend();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.pose().pushPose();
        this.textOffsetY = org.joml.Math.lerp(this.textOffsetY, this.centerY+Math.round(-this.scroll)*36, partialTick);
        guiGraphics.pose().translate(this.centerX, this.textOffsetY, 0);
        guiGraphics.pose().scale(1.5f,1.5f,1.5f);
        for (int i=0; i<list.size(); i++) {
            int distance = Math.toIntExact(Math.abs(i - Math.round(this.scroll)));
            float y = i*24-this.font.lineHeight/2f;

            guiGraphics.pose().pushPose();
            RenderSystem.enableBlend();
            guiGraphics.setColor(1,1,1,1-(distance*0.33f));
            guiGraphics.pose().translate(0, y, 0);
            guiGraphics.drawCenteredString(this.font, list.get(i), 0, 0, 0xFFFFFF);
            guiGraphics.setColor(1,1,1,1);
            RenderSystem.disableBlend();
            guiGraphics.pose().popPose();
        }
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double toSet = Math.min(this.list.size()-1, Math.max(this.scroll-scrollY, 0));
        if (this.scroll != toSet)
            this.playClickSound(2f);
        this.scroll = toSet;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void playClickSound(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (!this.list.isEmpty())
                PacketDistributor.sendToServer(new ConfigureStrippedCablePacket(this.list.get((int) Math.round(this.scroll)), this.menu.pos));
            this.playClickSound(1f);
            this.onClose();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
