package moth.boxxed.panels.api.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class HotbarOverlayManager {
    private static final ResourceLocation OVERLAY_TEXTURE = Dashpanels.path("textures/gui/hud/module_highlight.png");

    public static boolean shouldRender() {
        HitResult result = Minecraft.getInstance().hitResult;
        Level level = Minecraft.getInstance().level;
        if (level != null && result instanceof BlockHitResult blockHitResult) {
            return level.getBlockEntity(blockHitResult.getBlockPos()) instanceof AbstractPanelBlockEntity;
        }
        return false;
    }

    public static void renderOverlay(List<ItemStack> hotbarItems, ItemStack offhandItem, HumanoidArm offhandArm, GuiGraphics graphics, int cornerX, int cornerY) {
        RenderSystem.enableBlend();

        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.translate(0, 0, 1f);
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = hotbarItems.get(i);
            if (ModuleType.isRegisteredModule(itemStack.getItem())) {
                renderIndicator(graphics, cornerX+1+i*20, cornerY+2);
            }
        }

        if (ModuleType.isRegisteredModule(offhandItem.getItem())) {
            if (offhandArm == HumanoidArm.LEFT) {
                renderIndicator(graphics, cornerX-28, cornerY+2);
            } else {
                renderIndicator(graphics, cornerX+190, cornerY+2);
            }
        }
        stack.popPose();
        RenderSystem.disableBlend();
    }

    private static void renderIndicator(GuiGraphics graphics, int x, int y) {
        double d0 = Util.getMillis()/300f;
        int vOffset = (int) (Math.clamp(Mth.positiveModulo(Math.round(d0), 16), 0, 15)*18);
        double alpha = Mth.map(Math.sin(d0), -1, 1, 0.5, 1);
        graphics.setColor(1F, 1F, 1F, (float) alpha);
        graphics.blit(OVERLAY_TEXTURE,x, y, 0, vOffset, 18, 18, 18, 288);
        graphics.setColor(1, 1, 1, 1);
    }
}
