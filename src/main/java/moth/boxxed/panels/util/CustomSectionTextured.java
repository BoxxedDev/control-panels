package moth.boxxed.panels.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mcexpanded.fancytabsections.Section.SectionTextured;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class CustomSectionTextured extends SectionTextured {
    int secondaryTextColor = 0xC2C2C2;

    int horizontalSize = 162;
    int verticalSize = 18;

    public CustomSectionTextured(ResourceLocation id) {
        super(id);
    }

    public CustomSectionTextured setSecondaryTextColor(int color) {
        this.secondaryTextColor = color;
        return this;
    }

    @Override
    public SectionTextured setHorizontalSize(int horizontalSize) {
        this.horizontalSize = horizontalSize;
        return super.setHorizontalSize(horizontalSize);
    }

    @Override
    public SectionTextured setVerticalSize(int verticalSize) {
        this.verticalSize = verticalSize;
        return super.setVerticalSize(verticalSize);
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, int topLeftX, int topLeftY) {
        super.render(guiGraphics, font, topLeftX, topLeftY);
        this.renderTitle(guiGraphics, font, topLeftX, topLeftY);
    }

    @Override
    public void renderTitle(GuiGraphics guiGraphics, Font font, int topLeftX, int topLeftY) {
        topLeftX += titleOffsetX;
        topLeftY += titleOffsetY;

        int strCenterX = topLeftX+78;

        int minX = strCenterX-(font.width(title)/2 + 5);
        int minY = topLeftY-2;
        int maxX = strCenterX+(font.width(title)/2 + 5);
        int maxY = topLeftY+font.lineHeight+1;

        RenderSystem.enableBlend();
        guiGraphics.fill(minX, minY-1, maxX, maxY+1, 0x14000000);
        guiGraphics.fill(minX-1, minY, maxX+1, maxY, 0x14000000);
        RenderSystem.disableBlend();

        guiGraphics.drawCenteredString(font, title, strCenterX, topLeftY, this.textColor);

        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        stack.translate(0, 0, 1);

        int halfSizeY = font.lineHeight/2;
        guiGraphics.enableScissor(topLeftX, topLeftY+halfSizeY, topLeftX+horizontalSize, topLeftY+verticalSize);
        guiGraphics.drawCenteredString(font, title, strCenterX, topLeftY, this.secondaryTextColor);
        guiGraphics.disableScissor();

        stack.popPose();
    }
}