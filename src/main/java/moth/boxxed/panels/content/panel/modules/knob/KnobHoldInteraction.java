package moth.boxxed.panels.content.panel.modules.knob;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import moth.boxxed.panels.network.packet.DefaultModuleUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class KnobHoldInteraction extends ModuleHoldInteraction<KnobModule> {
    private static final ResourceLocation KNOB_SPRITE = ControlPanels.path("module/knob");

    private float val = 0;

    private int oldAngle = 0;
    private int angle = 0;

    private Module knobModule;
    private Level level;
    private Player player;

    @Override
    public void startHold(Level level, Player player, KnobModule module) {
        super.startHold(level, player, module);
        this.angle = module.getAngle();
        this.val = this.angle/360f;
        this.knobModule = module;
        this.level = level;
        this.player = player;
    }

    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        this.val += (float) (yaw/360f);
        this.val = Math.clamp(this.val, 0, 1);
        this.angle = Math.clamp(Math.round(this.val*360), 0, 360);
        if (this.oldAngle != angle) {
            PacketDistributor.sendToServer(new DefaultModuleUpdatePacket(this.knobModule.getParentPos(), this.knobModule.getName(), this.angle));
        }
        this.oldAngle = angle;
        return true;
    }

    @Override
    public void renderGui(GuiGraphics graphics, float partialTick) {
        int section = Math.round(Mth.map(angle, 0, 360, 0, 16));

        int centerX = graphics.guiWidth()/2;
        int centerY = graphics.guiHeight()/2;
        int x = centerX-9;
        int y = centerY-8;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        graphics.blitSprite(KNOB_SPRITE, 289, 17, section*17, 0, x, y, 17, 17);
        graphics.drawCenteredString(Minecraft.getInstance().font, String.valueOf(this.angle), centerX, centerY+12, 0xFFFFFFFF);
        RenderSystem.disableBlend();
    }
}
