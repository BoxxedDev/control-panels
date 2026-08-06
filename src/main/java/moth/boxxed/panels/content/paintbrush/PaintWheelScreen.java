package moth.boxxed.panels.content.paintbrush;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSoundEvents;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.DashpanelsClient;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.api.panel.skin.ClientSkin;
import moth.boxxed.panels.api.panel.skin.PanelSkinsClientManager;
import moth.boxxed.panels.api.panel.skin.ServerSkin;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.network.packet.SetPanelSkinPacket;
import moth.boxxed.panels.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

//TODO: add narration stuff
public class PaintWheelScreen extends Screen {
    public static final ResourceLocation PAINT_WHEEL = Dashpanels.path("textures/gui/paint_brush/paint_wheel.png");

    private final List<SkinButton> skins = new ArrayList<>();
    private final List<ClientSkin> availableSkins = new ArrayList<>();
    private ClientSkin currentSkin;

    private BlockPos pos;
    private PanelType type;

    private int currentPage;
    private int pages;

    private int centerX;
    private int centerY;

    private PageChangeButton leftChange;
    private PageChangeButton rightChange;

    private ApplyButton applyButton;
    private ApplyButton applyToAllButton;

    private ColorPicker colorPicker;
    private HexInput hexInput;
    private final List<PaletteColorButton> paletteColors = new ArrayList<>();
    private PaletteColorButton selectedColor;

    private GenericPaletteButton savePaletteButton;
    private GenericPaletteButton loadPaletteButton;
    private GenericPaletteButton addColorButton;
    private GenericPaletteButton deleteColorButton;

    public PaintWheelScreen(ServerSkin skinsToDisplay, BlockPos clickedPos) {
        super(Component.literal("Paint Wheel"));

        if (!(Minecraft.getInstance().level.getBlockEntity(clickedPos) instanceof AbstractPanelBlockEntity pbe)) {
            return;
        }

        for (ResourceLocation location : skinsToDisplay.skinsList()) {
            if (!PanelSkinsClientManager.MAP.containsKey(location) || PanelSkinsClientManager.MAP.get(location) == null) continue;
            this.availableSkins.add(PanelSkinsClientManager.MAP.get(location));
        }

        ResourceLocation skin = pbe.skin;
        if (PanelSkinsClientManager.MAP.containsKey(skin) && PanelSkinsClientManager.MAP.get(skin) != null) {
            this.currentSkin = PanelSkinsClientManager.MAP.get(skin);
        }

        this.pos = clickedPos;
        this.type = skinsToDisplay.type();

        this.currentPage = 1;
        this.pages = Math.ceilDiv(skins.size(), 8)+1;

        if (this.pages > 1) {
            this.leftChange = this.addWidget(new PageChangeButton(-1, Component.translatable("dashpanels.paint_wheel.left_change")));
            this.rightChange = this.addWidget(new PageChangeButton(1, Component.translatable("dashpanels.paint_wheel.right_change")));
        }

        this.applyButton = this.addWidget(new ApplyButton(false, Component.translatable("dashpanels.paint_wheel.apply")));
        this.applyToAllButton = this.addWidget(new ApplyButton(true, Component.translatable("dashpanels.paint_wheel.apply_all")));

        this.colorPicker = this.addWidget(new ColorPicker(pbe.skinColor, Component.translatable("dashpanels.paint_wheel.color_picker")));
        this.hexInput = this.addWidget(
                new HexInput(
                        Minecraft.getInstance().font,
                        66, 15,
                        Component.translatable("dashpanels.paint_wheel.hex_input")
                )
        );
//        this.hexInput.setFilter(s -> Pattern.compile("^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$").matcher(s).matches());
        this.reconstructPaletteColors(DashpanelsClient.PALETTE_STORAGE.getDefaultPalette());

        this.savePaletteButton = this.addWidget(new ExportPaletteButton(
                Component.translatable("dashpanels.paint_wheel.save_palette"),
                32,
                null
        ));

        this.loadPaletteButton = this.addWidget(
                new ImportPaletteButton(
                        Component.translatable("dashpanels.paint_wheel.load_palette"),
                        64,
                        null
                )
        );

        this.addColorButton = this.addWidget(new GenericPaletteButton(
                Component.translatable("dashpanels.paint_wheel.add_color"),
                96,
                () -> {
                    if (this.selectedColor != null) {
                        this.selectedColor.color = this.colorPicker.getColor();
                    } else if (this.paletteColors.size() < ColorPalette.MAX){
                        this.paletteColors.add(
                                this.addWidget(
                                        new PaletteColorButton(this.colorPicker.getColor(), Component.translatable("dashpanels.paint_wheel.palette_color"))
                                )
                        );
                    }
                }
        ));

        this.deleteColorButton = this.addWidget(new GenericPaletteButton(
                Component.translatable("dashpanels.paint_wheel.delete_color"),
                128,
                () -> {
                    if (this.selectedColor != null) {
                        this.removeWidget(this.selectedColor);
                        this.paletteColors.remove(this.selectedColor);
                        this.selectedColor = null;
                    }
                }
        ));

        this.reconstructSkinWidgets();
    }

    private void reconstructSkinWidgets() {
        if (!this.skins.isEmpty()) {
            for (SkinButton button : this.skins) {
                this.removeWidget(button);
            }
        }

        this.skins.clear();
        for (int i = (currentPage-1)*8; i < (currentPage)*8; i++) {
            ClientSkin skin = i>=this.availableSkins.size() ? null : this.availableSkins.get(i);
            this.skins.add(
                    this.addWidget(
                            new SkinButton(
                                    skin,
                                    Component.empty()
                            )
                    )
            );
        }
    }

    private void reconstructPaletteColors(ColorPalette palette) {
        if (palette == null)
            return;

        if (!this.paletteColors.isEmpty()) {
            for (PaletteColorButton button : this.paletteColors) {
                this.removeWidget(button);
            }
            this.paletteColors.clear();
        }

        for (int color : palette) {
            this.paletteColors.add(
                    this.addWidget(
                            new PaletteColorButton(color, Component.translatable("dashpanels.paint_wheel.palette_color"))
                    )
            );
        }
        this.selectedColor = null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBlurredBackground(partialTick);

        this.centerX = guiGraphics.guiWidth()/2;
        this.centerY = guiGraphics.guiHeight()/2;

        this.renderSkins(guiGraphics, mouseX, mouseY, partialTick);
        this.renderExtra(guiGraphics, mouseX, mouseY, partialTick);
        this.renderColorPicker(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderSkins(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(PAINT_WHEEL, centerX-14, centerY-14, 0, 0, 28, 28, 256, 256);
        ItemStack itemStack = new ItemStack(type.block);
        guiGraphics.renderItem(itemStack, centerX-8, centerY-8);

        int radii = 70;
        for (int i = 0; i < this.skins.size(); i++) {
            float angle = i*45;
            angle -= 90;
            int x = ((int) (Math.cos(angle* Mth.DEG_TO_RAD)*radii)) + this.centerX;
            int y = ((int) (Math.sin(angle*Mth.DEG_TO_RAD)*radii)) + this.centerY;

            SkinButton button = this.skins.get(i);
            button.setX(x-14);
            button.setY(y-14);
            button.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderExtra(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.pages > 1) {
            this.leftChange.setX(this.centerX-55);
            this.rightChange.setX(this.centerX+40);

            this.leftChange.renderWidget(graphics, mouseX, mouseY, partialTick);
            this.rightChange.renderWidget(graphics, mouseX, mouseY, partialTick);

            graphics.drawCenteredString(this.font, "%d / %d".formatted(this.currentPage, this.pages), this.centerX, 24, 0xFFFFFFFF);
        }

        this.applyButton.setX(this.centerX+115);
        this.applyButton.setY(this.centerY-36);
        this.applyButton.renderWidget(graphics, mouseX, mouseY, partialTick);

        this.applyToAllButton.setX(this.centerX+115);
        this.applyToAllButton.setY(this.centerY+20);
        this.applyToAllButton.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    private void renderColorPicker(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.currentSkin.tintable().orElse(false)) {
            int left = 10;
            int top = this.centerY-80;

            graphics.blit(PAINT_WHEEL, left, top, 32, 16, 102, 113, 256, 256);

            this.colorPicker.setX(left+6);
            this.colorPicker.setY(top+6);
            this.colorPicker.renderWidget(graphics, mouseX, mouseY, partialTick);

            this.hexInput.setX(left+6);
            this.hexInput.setY(top+92);
            this.hexInput.renderWidget(graphics, mouseX, mouseY, partialTick);

            this.savePaletteButton.setX(left+78);
            this.savePaletteButton.setY(top+26);
            this.savePaletteButton.renderWidget(graphics, mouseX, mouseY, partialTick);

            this.loadPaletteButton.setX(left+78);
            this.loadPaletteButton.setY(top+47);
            this.loadPaletteButton.renderWidget(graphics, mouseX, mouseY, partialTick);

            this.addColorButton.setX(left+78);
            this.addColorButton.setY(top+68);
            this.addColorButton.renderWidget(graphics, mouseX, mouseY, partialTick);

            this.deleteColorButton.setX(left+78);
            this.deleteColorButton.setY(top+89);
            this.deleteColorButton.renderWidget(graphics, mouseX, mouseY, partialTick);

            graphics.pose().pushPose();
            //Line it up ever so slightly...
            graphics.pose().translate(0.5f, 0, 0);
            int width = 6;
            for (int i = 0; i < this.paletteColors.size(); i++) {
                int x = Math.floorMod(i, width)*17;
                int y = (int) (Math.floor((double) i / width)) * 17;

                PaletteColorButton button = this.paletteColors.get(i);
                button.setX(left+x);
                button.setY(top+122+y);
                button.renderWidget(graphics, mouseX, mouseY, partialTick);
            }
            graphics.pose().popPose();
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

//    @Override
//    public void onClose() {
//        this.close(false);
//    }

    private void close(boolean applyToAll) {
        ResourceLocation skin = this.type.defaultSkin;
        for (Map.Entry<ResourceLocation, ClientSkin> entry : PanelSkinsClientManager.MAP.entrySet()) {
            if (entry.getValue().equals(this.currentSkin)) {
                skin = entry.getKey();
                break;
            }
        }

        int color = this.currentSkin.tintable().orElse(false) ? this.colorPicker.getColor() : 0xFFFFFF;
        PacketDistributor.sendToServer(new SetPanelSkinPacket(this.type, this.pos, skin, Optional.of(color), applyToAll));
        super.onClose();
    }

    public class ApplyButton extends AbstractWidget {
        private final boolean toAll;

        public ApplyButton(boolean toAll, Component message) {
            super(0, 0, 80, 16, message);
            this.toAll = toAll;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blit(PAINT_WHEEL, this.getX(), this.getY(), 144, 96, this.getWidth(), this.getHeight());
            guiGraphics.drawCenteredString(PaintWheelScreen.this.font, this.getMessage(), this.getX()+this.getWidth()/2, this.getY()+4, 0xFFFFFF);
            if (this.isMouseOver(mouseX, mouseY)) {
                RenderSystem.enableBlend();
                guiGraphics.setColor(1, 1, 1, 0.25f);
                guiGraphics.fill(this.getX()+1, this.getY()+1, this.getX()+this.getWidth()-1, this.getY()+this.getHeight()-1, 1, 0xFFFFFFFF);
                guiGraphics.setColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            PaintWheelScreen.this.close(this.toAll);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }

    public class SkinButton extends AbstractWidget {
        protected final ClientSkin skin;
        protected final ResourceLocation skinLocation;
        protected float scale = 1;

        public SkinButton(ClientSkin skin, Component message) {
            super(0, 0, 28, 28, message);
            this.skin = skin;
            this.skinLocation = PanelSkinsClientManager.REVERSE_MAP.get(skin);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            PoseStack poseStack = guiGraphics.pose();

            poseStack.pushPose();
            this.scale = Mth.lerp(partialTick, this.scale, this.isMouseOver(mouseX, mouseY) || PaintWheelScreen.this.currentSkin == this.skin ? 1.25f : 1f);
            MathUtil.scaleAround(poseStack, this.scale, this.scale, 1, this.getX()+14, this.getY()+14, 0);
            poseStack.translate(this.getX(), this.getY(), 0);
            guiGraphics.blit(PAINT_WHEEL, 0, 0, 0, 0, 28, 28, 256, 256);
            poseStack.popPose();

            if (this.skin != null) {
                BakedModel model = this.skin.getItemBakedModel();
                renderNonExistentItem(model, guiGraphics, this.getX()+6, this.getY()+6);

                if (this.isMouseOver(mouseX, mouseY)) {
                    List<Component> components = new ArrayList<>();
                    components.add(Component.translatableWithFallback("%s.paint_wheel.skin.%s"
                            .formatted(this.skinLocation.getNamespace(),
                                    this.skinLocation.getPath().replace('/', '.')
                            ),
                            this.skinLocation.toString()
                    ));
                    if (this.skin.author().isPresent()) {
                        components.add(Component.empty());
                        components.add(Component.translatable("dashpanels.paint_wheel.author", this.skin.author().orElse("")));
                    }
                    guiGraphics.renderTooltip(PaintWheelScreen.this.font, components, Optional.empty(), mouseX, mouseY);
                }
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            if (this.skin == null) {
                return;
            }
            PaintWheelScreen.this.currentSkin = this.skin;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }

    public class PageChangeButton extends AbstractWidget {
        private final int amountChange;

        public PageChangeButton(int amountChange, Component message) {
            super(0, 20, 15, 15, message);
            this.amountChange = amountChange;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int uOffset = this.amountChange < 0 ? 32 : 48;
            guiGraphics.blit(PAINT_WHEEL, this.getX(), this.getY(), uOffset, 0, this.getWidth(), this.getHeight(), 256, 256);
            if (this.isMouseOver(mouseX, mouseY)) {
                RenderSystem.enableBlend();
                guiGraphics.setColor(1, 1, 1, 0.25f);
                guiGraphics.fill(this.getX(), this.getY(), this.getX()+this.getWidth(), this.getY()+this.getHeight(), 1, 0xFFFFFFFF);
                guiGraphics.setColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            PaintWheelScreen.this.currentPage = Math.clamp(PaintWheelScreen.this.currentPage+this.amountChange, 0, PaintWheelScreen.this.pages);
            PaintWheelScreen.this.reconstructSkinWidgets();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }

    public class ColorPicker extends AbstractWidget {
        private float hue = 0;
        private float saturation = 0;
        private float brightness = 1;

        private final PickerGradient gradient = new PickerGradient(66, 66, 0.0f);
        private final HueGradient hueGradient = new HueGradient(66, 4);

        public ColorPicker(int color, Component message) {
            super(0, 0, 66, 82, message);

            this.setColor(color);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.gradient.render(guiGraphics, this.getX(), this.getY());
            this.hueGradient.render(guiGraphics, this.getX(), this.getY()+this.getHeight()-8);

            int satMapped = (int) (this.getX()+Mth.clampedMap(this.saturation, 0, 1, 0, 64));
            int hueMapped = (int) (this.getX()+Mth.clampedMap(this.hue, 0, 1, 0, 64));
            int alphaMapped = (int) (this.getX()+Mth.clampedMap(this.alpha, 0, 1, 0, 64));
            int brightMapped = (int) (this.getY()+Mth.clampedMap(this.brightness, 1, 0, 0, 64));

            guiGraphics.blit(PAINT_WHEEL, satMapped, this.getY(), 144, 16, 3, 66);
            guiGraphics.blit(PAINT_WHEEL, this.getX(), brightMapped, 160, 16, 66, 3);

            guiGraphics.blit(PAINT_WHEEL, hueMapped, this.getY()+this.getHeight()-8, 144, 0, 3, 4);
            guiGraphics.blit(PAINT_WHEEL, alphaMapped, this.getY()+this.getHeight()-4, 144, 4, 3, 4);

            int color = 0xFF000000 | Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 0xFFFFFF;
            guiGraphics.fill(this.getX()+this.getWidth()+8, this.getY(), this.getX()+this.getWidth()+24, this.getY()+16, color);
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            if (mouseX >= this.getX()-1 && mouseX <= this.getX()+this.getWidth()+1) {
                float x = (float) Mth.clampedMap(mouseX, this.getX(), this.getX()+this.getWidth(), 0, 1);
                if (mouseY >= this.getY()-1 && mouseY <= this.getY()+67) {
                    this.saturation = x;
                    this.brightness = (float) Mth.clampedMap(mouseY, this.getY(), this.getY()+66, 1, 0);
                } else if (mouseY >= this.getY()+this.getHeight()-9 && mouseY <= this.getY()+this.getHeight()-3) {
                    this.hue = x;
                    this.gradient.setHue(this.hue);
                }
            }
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            this.onClick(mouseX, mouseY, 0);
        }

        public void setColor(int color) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            this.hue = hsb[0];
            this.saturation = hsb[1];
            this.brightness = hsb[2];

            this.gradient.setHue(this.hue);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        public int getColor() {
            return Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
        }
    }

    public class HexInput extends EditBox {
        public HexInput(Font font, int width, int height, Component message) {
            super(font, width, height, message);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                try {
                    int color = Integer.decode(this.getValue());
                    PaintWheelScreen.this.colorPicker.setColor(color);
                    return true;
                } catch (NumberFormatException e) {}

                return true;
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    //A little bloated but it's wtv
    private static class PickerGradient implements AutoCloseable {
        private static final ResourceLocation LOCATION = Dashpanels.path("textures/gui/paint_brush/picker_gradient");

        private boolean dirty = true;

        private float hue;

        private final int width;
        private final int height;

        private final DynamicTexture texture;

        public PickerGradient(int width, int height, float hue) {
            this.width = width;
            this.height = height;
            this.hue = hue;

            TextureManager manager = Minecraft.getInstance().getTextureManager();

            this.texture = new DynamicTexture(width*4, height*4, false);
            this.texture.setFilter(true, false);

            manager.register(LOCATION, this.texture);

            this.generateTexture();
        }

        private void generateTexture() {
            NativeImage image = this.texture.getPixels();

            if (image == null) {
                return;
            }

            int hsbColor = Color.HSBtoRGB(this.hue, 1, 1);

            for (int y = 0; y < this.height*4; y++) {
                float v = y/(this.height*4-1f);
                for (int x = 0; x < this.width*4; x++) {
                    float u = x/(this.width*4-1f);
                    int color = bilinear(hsbColor, u, v);
                    image.setPixelRGBA(x, y, color);
                }
            }

            this.texture.upload();
            this.dirty = false;
        }

        public void render(
                GuiGraphics graphics,
                int x, int y
        ) {
            if (this.dirty)
                generateTexture();

            graphics.blit(
                    LOCATION,
                    x, y,
                    this.width, this.height,
                    0, 0,
                    this.width*4, this.height*4,
                    this.width*4, this.height*4
            );
        }

        public void setHue(float hue) {
            this.hue = hue;
            this.dirty = true;
        }

        private static int bilinear(int color, float u, float v) {
            float r = (color >> 16) & 0xFF;
            float g = (color >> 8) & 0xFF;
            float b = color & 0xFF;

            float topR = Mth.lerp(u, 255, r);
            float topG = Mth.lerp(u, 255, g);
            float topB = Mth.lerp(u, 255, b);

            int outR = Math.clamp(Math.round(Mth.lerp(v, topR, 0)), 0, 255);
            int outG = Math.clamp(Math.round(Mth.lerp(v, topG, 0)), 0, 255);
            int outB = Math.clamp(Math.round(Mth.lerp(v, topB, 0)), 0, 255);

            //For some reason the output is flipped? it's weird
            return 0xFF000000 | (outB << 16) | (outG << 8) | outR;
        }

        @Override
        public void close() throws Exception {
            this.texture.close();
        }
    }

    private static class HueGradient implements AutoCloseable {
        private static final ResourceLocation LOCATION = Dashpanels.path("textures/gui/paint_brush/hue_gradient");

        private final DynamicTexture texture;

        private final int width;
        private final int height;

        private boolean dirty = true;

        public HueGradient(int width, int height) {
            TextureManager manager = Minecraft.getInstance().getTextureManager();

            this.texture = new DynamicTexture(width*4, height, false);
            this.texture.setFilter(true, false);

            manager.register(LOCATION, this.texture);

            this.width = width;
            this.height = height;

            this.generateTexture();
        }

        private void generateTexture() {
            NativeImage image = this.texture.getPixels();

            if (image == null)
                return;

            for (int x = 0; x < width*4; x++) {
                int color = Color.HSBtoRGB(Mth.clampedMap(x, 0, width*4, 0f, 1f), 1f, 1f);

                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;

                color = 0xFF000000 | (b << 16) | (g << 8) | r;
                for (int y = 0; y < height; y++) {
                    image.setPixelRGBA(x, y, color);
                }
            }

            this.texture.upload();

            this.dirty = false;
        }

        public void render(GuiGraphics graphics, int x, int y) {
            if (this.dirty)
                this.generateTexture();

            graphics.blit(
                    LOCATION,
                    x, y,
                    this.width, this.height,
                    0, 0,
                    this.width*4, this.height*4,
                    this.width*4, this.height*4
            );
        }

        @Override
        public void close() throws Exception {
            this.texture.close();
        }
    }

    public class PaletteColorButton extends AbstractWidget {
        protected int color;
        protected float scale = 1;

        public PaletteColorButton(int color, Component message) {
            super(0, 0, 16, 16, message);
            this.color = color;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            PoseStack poseStack = guiGraphics.pose();

            poseStack.pushPose();
            this.scale = Mth.lerp(partialTick, this.scale, this.isMouseOver(mouseX, mouseY) || PaintWheelScreen.this.selectedColor == this ? 1.1f : 1f);
            MathUtil.scaleAround(poseStack, this.scale, this.scale, 1, this.getX()+8, this.getY()+8, 0);
            poseStack.translate(this.getX(), this.getY(), 0);
            guiGraphics.blit(PAINT_WHEEL, 0, 0, 64, 0, 16, 16, 256, 256);
            guiGraphics.fill(5, 5, this.getWidth()-5, this.getHeight()-5, 0xFF000000 | this.color);
            poseStack.popPose();
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            PaintWheelScreen.this.colorPicker.setColor(this.color);

            if (PaintWheelScreen.this.selectedColor != this) {
                PaintWheelScreen.this.selectedColor = this;
            } else {
                PaintWheelScreen.this.selectedColor = null;
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }

    public class GenericPaletteButton extends AbstractWidget {
        private final int vOffset;
        private final Runnable onClick;

        public GenericPaletteButton(Component message, int vOffset, Runnable onClick) {
            super(0, 0, 20, 20, message);
            this.vOffset = vOffset;
            this.onClick = onClick;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blit(PAINT_WHEEL, this.getX(), this.getY(), 0, this.vOffset, this.getWidth(), this.getHeight());

            if (this.isMouseOver(mouseX, mouseY)) {
                RenderSystem.enableBlend();
                guiGraphics.setColor(1, 1, 1, 0.25f);
                guiGraphics.fill(this.getX()+1, this.getY()+1, this.getX()+this.getWidth()-1, this.getY()+this.getHeight()-1, 1, 0xFFFFFFFF);
                guiGraphics.setColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            this.onClick.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }

    public class ImportPaletteButton extends GenericPaletteButton {
        private ValueScrollingWidget widget;
        private boolean toggled = false;

        public ImportPaletteButton(Component message, int vOffset, Runnable onClick) {
            super(message, vOffset, onClick);
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            this.toggled = !this.toggled;
            this.toggleWidget();

            Dashpanels.LOGGER.debug(String.valueOf(this.toggled));
        }

        private void toggleWidget() {
            if (this.toggled) {
                Map<String, ColorPalette> paletteFileMap = DashpanelsClient.PALETTE_STORAGE.allPalettesInFiles();

                this.widget = PaintWheelScreen.this.addWidget(
                        new ValueScrollingWidget(
                                paletteFileMap.keySet(),
                                Component.translatable(""),
                                (index, values) -> {
                                    Map<String, ColorPalette> map = DashpanelsClient.PALETTE_STORAGE.allPalettesInFiles();
                                    if (index >= 0) {
                                        PaintWheelScreen.this.reconstructPaletteColors(map.get(values.get(index)));
                                    }
                                }
                        )
                );
            } else {
                PaintWheelScreen.this.removeWidget(this.widget);
                this.widget = null;
            }
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

            if (this.widget != null) {
                this.widget.setX(this.getX()+25);
                this.widget.setY(this.getY()-2);
                this.widget.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public void setFocused(boolean focused) {
//            if (!focused && this.widget != null) {
//                this.toggled = false;
//                toggleWidget();
//            }
//            super.setFocused(focused);
        }
    }

    public class ValueScrollingWidget extends AbstractWidget {
        private final BiConsumer<Integer, List<String>> onClick;
        private final List<String> values;
        private int index = -1;

        public ValueScrollingWidget(Collection<String> textValues, Component message, BiConsumer<Integer, List<String>> onClick) {
            super(0, 0, 80, 24, message);
            this.values = textValues.stream().toList();
            this.onClick = onClick;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blit(PAINT_WHEEL, this.getX(), this.getY(), 32, 144, this.getWidth(), this.getHeight());

            if (this.index >= 0) {
                guiGraphics.enableScissor(this.getX()+7, this.getY()+7, this.getX()+this.getWidth()-7, this.getY()+this.getHeight()-7);
                guiGraphics.drawScrollingString(PaintWheelScreen.this.font, Component.literal(this.values.get(this.index)), this.getX()+7, this.getX()+this.getWidth()-7, this.getY()+7, 0xFFFFFF);
                guiGraphics.disableScissor();
            }

            if (this.isMouseOver(mouseX, mouseY)) {
                List<Component> values = new ArrayList<>();
                for (int i = 0; i < this.values.size(); i++) {
                    String str = this.values.get(i);
                    if (i==this.index) {
                        values.add(Component.literal("-> "+str));
                    } else {
                        values.add(Component.literal(str).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }

                guiGraphics.renderTooltip(PaintWheelScreen.this.font, values, Optional.empty(), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (this.isMouseOver(mouseX, mouseY)) {
                int newIndex = (int) Math.clamp(this.index-scrollY, 0, this.values.size()-1);
                if (newIndex != this.index) {
                        Minecraft.getInstance()
                                .getSoundManager()
                                .play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(),
                                        1.5f + 0.1f * (this.index) / (this.values.size())));
                        this.index = newIndex;
                        return true;
                }
            }
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            this.onClick.accept(this.index, this.values);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }

    public class ExportPaletteButton extends GenericPaletteButton {
        private boolean toggled = false;
        private EditBox editBox;

        public ExportPaletteButton(Component message, int vOffset, Runnable onClick) {
            super(message, vOffset, onClick);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

            if (this.toggled && this.editBox != null) {
                guiGraphics.blit(PAINT_WHEEL, this.getX()+25, this.getY()-2, 32, 144, 80, 24);
                this.editBox.setX(this.getX()+31);
                this.editBox.setY(this.getY()+4);
                this.editBox.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            this.toggled = !this.toggled;

            if (this.toggled) {
                this.editBox = PaintWheelScreen.this.addWidget(
                        new EditBox(
                                PaintWheelScreen.this.font,
                                68, 12,
                                Component.translatable("")
                        )
                );
            } else {
                if (!this.editBox.getValue().isBlank()) {
                    ColorPalette palette = new ColorPalette();
                    for (PaletteColorButton paletteColor : PaintWheelScreen.this.paletteColors) {
                        palette.add(paletteColor.color);
                    }
                    DashpanelsClient.PALETTE_STORAGE.storePalette(this.editBox.getValue(), palette);
                }

                PaintWheelScreen.this.removeWidget(this.editBox);
                this.editBox = null;
            }
        }
    }
}
