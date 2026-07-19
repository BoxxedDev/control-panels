package moth.boxxed.panels.content.paintbrush;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.skin.ClientSkin;
import moth.boxxed.panels.api.panel.skin.PanelSkinsClientManager;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.api.panel.skin.ServerSkin;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.network.packet.SetPanelSkinPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaintWheel {
    private static boolean active = false;

    private static List<ClientSkin> skins;
    private static BlockPos pos;
    private static PanelType type;

    private static int currentPage;
    private static int pages;

    private static float selectedSection;

    private static int leftPos;
    private static int topPos;
    private static int centerX;
    private static int centerY;

    private static int mouseX;
    private static int mouseY;

    public static void open(ServerSkin skinsToDisplay, BlockPos clickedPos) {
        if (isActive()) return;

        skins = new ArrayList<>();
        for (ResourceLocation location : skinsToDisplay.skinsList()) {
            if (!PanelSkinsClientManager.MAP.containsKey(location) || PanelSkinsClientManager.MAP.get(location) == null) continue;
            skins.add(PanelSkinsClientManager.MAP.get(location));
        }
        pos = clickedPos;
        type = skinsToDisplay.type();

        active = true;

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        leftPos = (width - 127)/2;
        topPos = (height - 127)/2;

        centerX = width/2;
        centerY = height/2;

        mouseX = centerX;
        mouseY = centerY;

        currentPage = 1;
        pages = Math.ceilDiv(skins.size(), 8);

        selectedSection = 0;
    }

    public static void closeAndSend() {
        active = false;

        int i = Mth.positiveModulo(Math.round(selectedSection), 8) + (currentPage-1)*8;
        ResourceLocation skin = type.defaultSkin;
        if (i<skins.size()) {
            ClientSkin clientSkin = skins.get(i);
            for (Map.Entry<ResourceLocation, ClientSkin> entry : PanelSkinsClientManager.MAP.entrySet()) {
                if (entry.getValue().equals(clientSkin)) {
                    skin = entry.getKey();
                    break;
                }
            }
        }

        PacketDistributor.sendToServer(new SetPanelSkinPacket(pos, skin));
    }

    public static boolean beforeMouseInput(int button, int action) {
        if (Minecraft.getInstance().screen != null) return false;

        if (button==GLFW.GLFW_MOUSE_BUTTON_RIGHT && action==GLFW.GLFW_PRESS && isActive()) {
            closeAndSend();
            return true;
        }

        return false;
    }

    public static void moveMouse(double pitch, double yaw) {
        double clamped = Math.clamp(yaw/180f, -1, 1);
        selectedSection = (float) Mth.positiveModulo(selectedSection+clamped, 8);
    }

    public static boolean isActive() {
        return active;
    }

    private static final ResourceLocation GUI = Dashpanels.path("textures/gui/paint_brush/paint_brush.png");
    public static void render(GuiGraphics graphics, float partialTick) {
        renderBackground(graphics, partialTick);
        renderSkins(graphics, partialTick);
        renderExtraWidgets(graphics, partialTick);
    }

    private static void renderBackground(GuiGraphics graphics, float partialTick) {
        RenderSystem.enableBlend();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                int uOffset = j*127;
                int vOffset = i*127;
                int currentSection = (i*4)+j;

                float alpha = Mth.positiveModulo(Math.round(selectedSection), 8)==currentSection ? 0.6f : 0.4f;

                graphics.setColor(1f, 1f, 1f, alpha);
                graphics.blit(GUI, leftPos, topPos, uOffset, vOffset, 127, 127, 512, 512);
            }
        }
        graphics.setColor(1f, 1f, 1f, 0.4f);
        graphics.blit(GUI, leftPos, topPos, 0, 254, 127, 127, 512, 512);
        RenderSystem.disableBlend();
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    private static void renderExtraWidgets(GuiGraphics graphics, float partialTick) {
        Font font = Minecraft.getInstance().font;

        String text = String.format("%d / %d", currentPage, pages);
        graphics.drawCenteredString(font, text, centerX, topPos-16, 0xFFFFFF);
    }

    private static void renderSkins(GuiGraphics graphics, float partialTick) {
        ItemStack itemStack = new ItemStack(type.block);
        graphics.renderItem(itemStack, centerX-8, centerY-8);

        for (int i = (currentPage-1)*8; i < Math.min((currentPage)*8, skins.size()); i++) {
            float angle = i*45;
            angle -= 90;
            int x = ((int) (Math.cos(angle*Mth.DEG_TO_RAD)*50)) + centerX;
            int y = ((int) (Math.sin(angle*Mth.DEG_TO_RAD)*50)) + centerY;

            BakedModel model = skins.get(i).getItemBakedModel();
            renderNonExistentItem(model, graphics, x-8, y-8);
        }
    }

    private static void renderNonExistentItem(BakedModel bakedmodel, GuiGraphics graphics, int x, int y) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate((float)(x + 8), (float)(y + 8), (float)(150));

        try {
            pose.scale(16.0F, -16.0F, 16.0F);
            boolean flag = !bakedmodel.usesBlockLight();
            if (flag) {
                Lighting.setupForFlatItems();
            }

            Minecraft.getInstance()
                    .getItemRenderer()
                    .render(new ItemStack(PanelBlocks.CONTROL_PANEL), ItemDisplayContext.GUI, false, pose, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            graphics.flush();
            if (flag) {
                Lighting.setupFor3DItems();
            }
        } catch (Throwable throwable) {
        }

        pose.popPose();
    }
}