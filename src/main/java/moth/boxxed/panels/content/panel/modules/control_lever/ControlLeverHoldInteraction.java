package moth.boxxed.panels.content.panel.modules.control_lever;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import moth.boxxed.panels.network.packet.DefaultModuleUpdatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class ControlLeverHoldInteraction extends ModuleHoldInteraction<ControlLeverModule> {
    private static final ResourceLocation LEVER_SPRITE = ControlPanels.path("module/control_lever");

    private float val = 0;
    private int oldSignal = 0;
    private int signal = 0;

    private float renderSignal = 0;
    private float indicatorRender = 0;

    private ControlLeverModule leverModule;
    private Level level;

    @Override
    public void startHold(Level level, Player player, ControlLeverModule module) {
        super.startHold(level, player, module);
        this.signal = module.getSignal();
        this.val = this.signal/15f;
        this.leverModule = module;
        this.level = level;

        this.renderSignal = Mth.map((float) this.signal, 0, 15, 0, 112);
        this.indicatorRender = Mth.map((float) this.signal, 0, 15, 0, 112);
    }

    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        this.val -= (float) (pitch/180f);
        this.val = Math.clamp(this.val, 0, 1);
        this.signal = Math.clamp(Math.round(this.val*15), 0, 15);
        if (this.oldSignal != this.signal) {
            PacketDistributor.sendToServer(new DefaultModuleUpdatePacket(this.leverModule.getParentPos(), this.leverModule.getName(), this.signal));
        }
        this.oldSignal = this.signal;
        return true;
    }

    @Override
    public void tick() {
        this.renderSignal = org.joml.Math.lerp(this.renderSignal, Mth.map((float) this.signal, 0, 15, 0, 112), 0.5f);
        this.indicatorRender = org.joml.Math.lerp(this.indicatorRender, Mth.map((float) this.signal, 0, 15, 0,112), 0.15f);
    }

    @Override
    public void renderGui(GuiGraphics graphics, float partialTick) {
        int centerX = graphics.guiWidth()/2;
        int centerY = graphics.guiHeight()/2;
        int x = centerX - 128;
        int y = centerY - 64;

        graphics.blitSprite(LEVER_SPRITE, 128, 128, 0, 0, x, y, 0, 48, 128);
        graphics.blitSprite(LEVER_SPRITE, 128, 128, 48, 0, x, y+112-(Math.round(this.renderSignal)), 1, 32, 16);
        graphics.blitSprite(LEVER_SPRITE, 128, 128, 48, 16, x+33, y+116-(Math.round(this.indicatorRender)), 1, 14, 8);
    }
}
