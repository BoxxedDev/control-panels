package moth.boxxed.panels.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiUtil {
    //HOLY SHIT SO MANY ARGS
    public static void blitNineSlice(GuiGraphics graphics, ResourceLocation sprite,
                                     int x, int y, int width, int height,
                                     int uWidth, int vHeight, int uOffset, int vOffset,
                                     int textureWidth, int textureHeight,
                                     int top, int bottom, int left, int right) {

        //Top
        graphics.blit(sprite, x, y, uOffset, vOffset, left, top, textureWidth, textureHeight);
        graphics.blit(sprite, x+left, y, width-left-right, top, uOffset+left, vOffset, uWidth-left-right, top, textureWidth, textureHeight);
        graphics.blit(sprite, x+(width-right), y, uOffset+(uWidth-right), vOffset, right, top, textureWidth, textureHeight);

        //Bottom
        graphics.blit(sprite, x, y+(height-bottom), uOffset, vOffset+(vHeight-bottom), left, bottom, textureWidth, textureHeight);
        graphics.blit(sprite, x+left, y+(height-bottom), width-left-right, bottom, uOffset+left, vOffset+(vHeight-bottom), uWidth-left-right, bottom, textureWidth, textureHeight);
        graphics.blit(sprite, x+(width-right), y+(height-bottom), uOffset+(uWidth-right), vOffset+(vHeight-bottom), right, bottom, textureWidth, textureHeight);

        //Sides
        graphics.blit(sprite, x, y+top, left, height-top-bottom, uOffset, vOffset+top, left, vHeight-top-bottom, textureWidth, textureHeight);
        graphics.blit(sprite, x+(width-right), y+top, right, height-top-bottom, uOffset+(uWidth-right), vOffset+top, right, vHeight-top-bottom, textureWidth, textureHeight);

        //Middle
        graphics.blit(sprite, x+left, y+top, width-left-right, height-top-bottom, uOffset+left, vOffset+top, uWidth-left-right, vHeight-top-bottom, textureWidth, textureHeight);
    }

    public static void blitHorizontalTriSlice(GuiGraphics graphics, ResourceLocation sprite,
                                              int x, int y, int width, int height,
                                              int uWidth, int vHeight, int uOffset, int vOffset,
                                              int textureWidth, int textureHeight,
                                              int top, int bottom) {
        graphics.blit(sprite, x, y, uOffset, vOffset, width, top, textureWidth, textureHeight);
        graphics.blit(sprite, x, y+top, width, height-top-bottom, uOffset, vOffset+top, uWidth, vHeight-top-bottom, textureWidth, textureHeight);
        graphics.blit(sprite, x, y+height-bottom, uOffset, vOffset+vHeight-bottom, width, bottom, textureWidth, textureHeight);
    }
}
