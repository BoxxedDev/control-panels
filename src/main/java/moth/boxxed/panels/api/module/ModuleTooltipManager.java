package moth.boxxed.panels.api.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.Dashpanels;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TODO: Finish this
public class ModuleTooltipManager {
    private static final ResourceLocation DEFAULT_BG = Dashpanels.path("tooltip/background");
    private static final int padding = 4;

    public static void renderSelected(GuiGraphics graphics, float partialTicks, Module module, Vector3f moduleWorldPos) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        Font font = Minecraft.getInstance().font;
        Matrix4f cameraMatrix = gameRenderer.getProjectionMatrix(gameRenderer.getFov(camera, partialTicks, true));
        moduleWorldPos.sub((float) camera.getPosition().x, (float) camera.getPosition().y, (float) camera.getPosition().z);
        camera.rotation().transformInverse(moduleWorldPos);
        Vector4f worldPosToTransform = new Vector4f(moduleWorldPos.x, moduleWorldPos.y, moduleWorldPos.z, 1f).mul(cameraMatrix);
        float x = (-worldPosToTransform.x/worldPosToTransform.w)*(graphics.guiWidth()) + (graphics.guiWidth()/2f);
        float y = (-worldPosToTransform.y/worldPosToTransform.w)*(graphics.guiHeight()) + ((graphics.guiHeight()/2f));
        Dashpanels.LOGGER.debug("X : {} | Y : {}", x, y);
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.translate(x, y, 1);
        List<Component> list = new ArrayList<>();
        list.add(Component.literal(module.getName()));
        ResourceLocation bg = DEFAULT_BG;
        if (module instanceof IHoverTooltip tooltip) {
            tooltip.addLines(list);
            bg = tooltip.tooltipBackgroundSprite();
        }
        int width = padding*2;
        int height = padding;
        for (Component component : list) {
            int w = font.width(component)+padding*2;
            if (w > width)
                width = w;
            height += font.lineHeight+padding;
        }
        RenderSystem.enableBlend();
        graphics.setColor(1, 1, 1, 0.5f);
        graphics.blitSprite(bg, 0, 0, width, height);
        graphics.setColor(1, 1, 1, 1f);
        for (int i = 0; i < list.size(); i++) {
            graphics.drawString(font, list.get(i), padding, padding + i*font.lineHeight, 0xFFFFFF);
        }
        RenderSystem.disableBlend();

        stack.popPose();
    }
}
