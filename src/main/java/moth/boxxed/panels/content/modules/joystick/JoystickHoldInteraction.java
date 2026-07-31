package moth.boxxed.panels.content.modules.joystick;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class JoystickHoldInteraction extends ModuleHoldInteraction<JoystickModule> {
    private static final ResourceLocation CROSSHAIR = Dashpanels.path("module/joystick/crosshair");
    private static final ResourceLocation INDICATOR = Dashpanels.path("module/joystick/indicator");

    private float valX = 0;
    private float valY = 0;
    private boolean triggered = false;

    @Override
    public void start() {
        this.valX = 0;
        this.valY = 0;
        this.triggered = false;
        this.getGuiContext().setCrosshairVisibility(false);
    }

    @Override
    public void stop() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("stick_x", 0);
        tag.putFloat("stick_y", 0);
        tag.putBoolean("triggered", false);
        this.update(tag);
    }

    @Override
    public boolean attack(int action) {
        if (this.isActive()) {
            this.triggered = action==1;

            CompoundTag tag = new CompoundTag();
            tag.putFloat("stick_x", this.valX);
            tag.putFloat("stick_y", this.valY);
            tag.putBoolean("triggered", this.triggered);
            this.update(tag);
        }

        return super.attack(action);
    }

    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        float oldValX = valX;
        float oldValY = valY;

        this.valX += (float) (yaw/180f);
        this.valY -= (float) (pitch/180f);
        this.valX = Math.clamp(this.valX, -1, 1);
        this.valY = Math.clamp(this.valY, -1, 1);

        if (oldValX != this.valX || oldValY != this.valY) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("stick_x", this.valX);
            tag.putFloat("stick_y", this.valY);
            tag.putBoolean("triggered", this.triggered);
            this.update(tag);
        }

        return true;
    }

    @Override
    public void renderGui(GuiGraphics graphics, float partialTick) {
        int centerX = graphics.guiWidth()/2;
        int centerY = graphics.guiHeight()/2;

        int leftPos = centerX-11;
        int topPos = centerY-10;

        int indicatorX = (int) Mth.map(this.valX, -1, 1, leftPos-1, leftPos + 19);
        int indicatorY = (int) Mth.map(-this.valY, -1, 1, topPos-1, topPos + 19);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        graphics.blitSprite(CROSSHAIR, leftPos, topPos, 21, 21);
        graphics.blitSprite(INDICATOR, indicatorX, indicatorY, 3, 3);
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY+16, 0);
        graphics.pose().scale(0.5f,0.5f,0.5f);
        graphics.drawCenteredString(Minecraft.getInstance().font, "X: %.2f".formatted(this.valX), 0, 0, 0xAAFFCCCC);
        graphics.pose().translate(0, 12, 0);
        graphics.drawCenteredString(Minecraft.getInstance().font, "Y: %.2f".formatted(this.valY), 0, 0, 0xAACCFFCC);
        graphics.pose().popPose();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
