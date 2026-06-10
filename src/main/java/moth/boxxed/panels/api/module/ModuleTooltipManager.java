package moth.boxxed.panels.api.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.Dashpanels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;

//TODO: Finish this
public class ModuleTooltipManager {
    private static final ResourceLocation DEFAULT_BG = Dashpanels.path("tooltip/background");
    private static final int padding = 4;

    private static float tooltipTransparency = 0f;

    public static void renderSelected(GuiGraphics graphics, Module module) {
        Font font = Minecraft.getInstance().font;
        PoseStack stack = graphics.pose();
        List<Component> list = new ArrayList<>();
        list.add(Component.literal(module.getName()));
        ResourceLocation bg = DEFAULT_BG;
        if (module instanceof IHoverTooltip tooltip) {
            tooltip.addLines(list);
            bg = tooltip.tooltipBackgroundSprite();
        }

        int width = padding * 2;
        int height = padding + ((list.size()==1 ? 1 : list.size()-1)*(font.lineHeight+padding));
        for (Component component : list) {
            int w = font.width(component) + padding * 2;
            if (w > width)
                width = w;
        }

        stack.pushPose();
        float centerX = graphics.guiWidth()/2f;
        float centerY = graphics.guiHeight()/2f;
        stack.translate(centerX + 20, centerY, 0);
        stack.scale(0.5f, 0.5f, 1f);

        RenderSystem.enableBlend();

        graphics.setColor(1, 1, 1, tooltipTransparency);

        graphics.blitSprite(bg, 0,  -(height/2), width, height);
        for (int i = 0; i < list.size(); i++) {
            graphics.drawString(font, list.get(i), padding, -(height/2) + padding + (i * font.lineHeight), 0xFFFFFF);
        }

        graphics.setColor(1, 1, 1, 1f);

        RenderSystem.disableBlend();

        stack.popPose();
    }

    public static void guiFrame(boolean active) {
        if (active) {
            tooltipTransparency = Math.lerp(tooltipTransparency, 0.75f, 0.2f);
        } else {
            tooltipTransparency = Math.lerp(tooltipTransparency, 0f, 0.2f);
        }
    }
}
