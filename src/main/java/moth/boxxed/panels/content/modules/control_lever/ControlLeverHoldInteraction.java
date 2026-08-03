package moth.boxxed.panels.content.modules.control_lever;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ControlLeverHoldInteraction extends ModuleHoldInteraction<ControlLeverModule> {
    private static final ResourceLocation LEVER_SPRITE = Dashpanels.path("module/control_lever");

    private float val = 0;
    private int oldSignal = 0;
    private int signal = 0;

    private float renderSignal = 0;

    @Override
    public void start() {
        this.signal = module.getSignal();
        this.val = this.signal/15f;

        this.renderSignal = Mth.map((float) this.signal, 0, 15, 0, 112);
    }

    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        this.val -= (float) (pitch/180f);
        this.val = Math.clamp(this.val, 0, this.module.redstoneOutput.get()/15f);
        this.signal = Math.clamp(Math.round(this.val*15), 0, this.module.redstoneOutput.get());
        if (this.oldSignal != this.signal) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("signal", this.signal);
            this.update(tag);
        }
        this.oldSignal = this.signal;
        return true;
    }

    @Override
    public void tick() {
        this.renderSignal = org.joml.Math.lerp(this.renderSignal, Mth.map((float) this.signal, 0, 15, 0, 112), 0.5f);
    }

    @Override
    public void renderGui(GuiGraphics graphics, float partialTick) {
        int section = this.signal;

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
        graphics.blitSprite(LEVER_SPRITE, 272, 17, section*17, 0, x, y, 17, 17);
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY+12, 0);
        graphics.pose().scale(0.5f,0.5f,0.5f);
        graphics.drawCenteredString(Minecraft.getInstance().font, String.valueOf(this.signal), 0, 0, 0xAAFFFFFF);
        graphics.pose().popPose();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
