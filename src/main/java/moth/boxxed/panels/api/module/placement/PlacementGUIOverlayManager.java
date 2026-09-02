package moth.boxxed.panels.api.module.placement;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.util.GuiUtil;
import moth.boxxed.panels.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.util.List;

@ApiStatus.Internal
public class PlacementGUIOverlayManager {
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
        graphics.blit(OVERLAY_TEXTURE,x, y, 0, vOffset, 18, 18, 64, 288);
        graphics.setColor(1, 1, 1, 1);
    }

    public static void renderModuleMessage(GuiGraphics guiGraphics) {
        if (!PlacementManager.shouldRenderOverlay()) {
            return;
        }

        int centerX = guiGraphics.guiWidth()/2;
        int top = guiGraphics.guiHeight()-80;

        final Module activeModule = PlacementManager.getSelectedModule();

        final boolean hasRotation = activeModule.canRotate(new PlacementContext(
                Minecraft.getInstance().player,
                PlacementManager.isMovingModule() ? new ItemStack(ModuleType.getItemFromType(PlacementManager.getMovingModule().type)) : Minecraft.getInstance().player.getMainHandItem(),
                PlacementManager.isMovingModule()
        ));

        final Font font = Minecraft.getInstance().font;

        guiGraphics.pose().pushPose();
        MathUtil.scaleAround(guiGraphics.pose(), 1.1f, 1.1f, 1.1f, centerX, top, 0);

        RenderSystem.enableBlend();

        final List<ModuleType<?>> availableTypes = ModuleType.getTypesFromItem(PlacementManager.getSelectedItem());

        int width = 0;

        final MutableComponent moduleText = Component.empty();
        if (!PlacementManager.isMovingModule()) {
            moduleText.append(
                    Component.literal(ModulesRegistry.MODULE_REGISTRY.getKey(PlacementManager.getSelectedType()).toString())
                            .withStyle(PlacementManager.isChangeKeyDown() ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)
            );
            //Change to a translation thing like "2 More..."
            moduleText.append(Component.literal(" +%d".formatted(availableTypes.size()-1)).withStyle(ChatFormatting.DARK_GRAY));
        }

        final Module.Rotation rotation = PlacementManager.getRotation();
        final Component rotationText = Component.literal(String.valueOf(rotation.getAngle())).withStyle(ChatFormatting.BOLD);

        if (!PlacementManager.isMovingModule() && availableTypes.size() > 1) {
            width += font.width(moduleText)+5;
        }
        if (hasRotation) {
            width += 18 + font.width(rotationText);
        }

        if (width > 0) {
            int left = centerX-width/2;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, -1);
            GuiUtil.blitVerticalTriSlice(
                    guiGraphics, OVERLAY_TEXTURE,
                    left, top, width, 17,
                    20, 17, 30, 16,
                    64, 288,
                    4,4
                    );
            guiGraphics.pose().popPose();

            final int moduleTextWidth = !PlacementManager.isMovingModule() && availableTypes.size() > 1 ? font.width(moduleText)+6 : 0;
            if (!PlacementManager.isMovingModule() && availableTypes.size() > 1) {
                guiGraphics.drawString(font, moduleText, left+3, top+5, 0xFFFFFF, false);
            }
            if (hasRotation) {
                guiGraphics.blit(OVERLAY_TEXTURE, left + moduleTextWidth + 3, top+2, 50, 0, 11, 11, 64, 288);
                guiGraphics.drawString(font, rotationText, left+moduleTextWidth+16, top+5, 0xFFFFFF, false);
            }
        }

        RenderSystem.disableBlend();

        guiGraphics.pose().popPose();
    }
}
