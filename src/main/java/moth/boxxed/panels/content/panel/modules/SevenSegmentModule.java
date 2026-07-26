package moth.boxxed.panels.content.panel.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.io.IOutput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiConsumer;

public class SevenSegmentModule extends Module implements IOutput, IModuleLuaObject {
    public String display = "--";
    public DyeColor color = DyeColor.WHITE;

    public SevenSegmentModule(int x, int y) {
        super(PanelModules.SEVEN_SEGMENT.get(), x, y, 6, 4);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("display", this.display);
        tag.putInt("color", color.getId());
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.display = tag.getString("display");
        this.color = DyeColor.byId(tag.getInt("color"));
        return super.loadData(tag, registries);
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0, -1/32f, 0);
        PanelPreloadedModels.SEVEN_SEGMENT.render(poseStack, RenderType.solid(), packedLight);
        poseStack.popPose();

        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        poseStack.translate(0, 1/32f, 0);
        poseStack.scale(1/32f, 1/32f, 1/32f);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));

        float offset = this.display.length() == 1 ? 6 : 0;

        FormattedCharSequence sequence = Component.literal(this.display).getVisualOrderText();
        font.drawInBatch(
                sequence,
                -11.5f + offset, -7.5f,
                this.color.getTextColor(),
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                LightTexture.FULL_BRIGHT
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public ItemInteractionResult onItemUse(ItemStack stack, Level level, Player player) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            this.color = dyeItem.getDyeColor();
            return ItemInteractionResult.SUCCESS;
        }
        return super.onItemUse(stack, level, player);
    }

    @Override
    public VoxelShape getShape() {
        return Block.box(0, 0, 0, 6, 0.5, 4);
    }

    @Override
    public void setAnalog(int signal) {
        this.display = String.valueOf(signal);
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getDisplay", args -> this.display);
        consumer.accept("getColor", args -> this.color.getSerializedName());
        consumer.accept("setDisplay", args -> {
            if (args.count() != 1)
                return false;
            if (args.get(0) instanceof String str && str.length() <= 2) {
                this.display = str.isEmpty() ? "--" : str;
                this.parentBlockEntity.networkUpdate(this.parentBlockEntity.getOrCreate());
                return true;
            }
            return false;
        });
        consumer.accept("setColor", args -> {
            if (args.count() != 1)
                return false;
            if (args.get(0) instanceof String string) {
                this.color = DyeColor.byName(string, DyeColor.WHITE);
                this.parentBlockEntity.networkUpdate(this.parentBlockEntity.getOrCreate());
                return true;
            }
            return false;
        });
    }
}
