package moth.boxxed.panels.content.panel.modules.knob;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moth.boxxed.panels.Dashpanels;
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
    private static final ResourceLocation KNOB_SPRITE = Dashpanels.path("module/knob");

    private float val = 0;

    private int oldAngle = 0;
    private int angle = 0;

    @Override
    public void start() {
        this.angle = module.getAngle();
        this.val = this.angle/360f;
    }

    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        this.val += (float) (yaw/360f);
        this.val = Math.clamp(this.val, 0, 1);
        this.angle = Math.clamp(Math.round(this.val*360), 0, 360);
        if (this.oldAngle != angle) {
            this.update(this.angle);
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
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY+12, 0);
        graphics.pose().scale(0.5f,0.5f,0.5f);
        graphics.drawCenteredString(Minecraft.getInstance().font, String.valueOf(this.angle), 0, 0, 0xAAFFFFFF);
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }
}
