package moth.boxxed.panels.content.panel.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.IOutput;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PreLoadedModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiConsumer;

public class IndicatorBulbModule extends Module implements IOutput, IModuleLuaObject {
    public DyeColor color;
    public boolean lit;

    public IndicatorBulbModule(int x, int y) {
        super(PanelModules.INDICATOR_BULB.get(), x, y, 1,1);
        this.color = DyeColor.WHITE;
        this.lit = false;
    }

    @Override
    public void setAnalog(int signal) {
        this.lit = signal > 0;
    }

    @Override
    public void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PreLoadedModel bulbModel = this.lit ? PanelPreloadedModels.INDICATOR_BULB_ON.getModel(this.color) : PanelPreloadedModels.INDICATOR_BULB_OFF.getModel(this.color);

        Level level = panelBlockEntity.getLevel();
        BlockState state = panelBlockEntity.getBlockState();

        int bulbLight = this.lit ? LightTexture.FULL_BRIGHT : packedLight;
        PanelPreloadedModels.INDICATOR_BULB_BASE.render(level, state, poseStack, bufferSource, RenderType.solid(), packedLight);
        bulbModel.render(level, state, poseStack, bufferSource, RenderType.translucent(), bulbLight);
    }

    @Override
    public boolean loadData(CompoundTag tag) {
        this.color = DyeColor.byName(tag.getString("color"), DyeColor.WHITE);
        this.lit = tag.getBoolean("lit");
        return super.loadData(tag);
    }

    @Override
    public boolean saveData(CompoundTag tag) {
        tag.putString("color", this.color.getSerializedName());
        tag.putBoolean("lit", this.lit);
        return super.saveData(tag);
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
        return Block.box(0, 0, 0, 1, 1.5, 1);
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getState", args -> this.lit);
        consumer.accept("getColor", args -> this.color.getSerializedName());
        consumer.accept("setState", args -> {
            if (args.getType(0).equals("boolean")) {
                this.lit = (boolean) args.get(0);
                this.parentBlockEntity.networkUpdate(this.parentBlockEntity.getOrCreate());
                return true;
            }
            return false;
        });
        consumer.accept("setColor", args -> {
            if (args.getType(0).equals("string")) {
                this.color = DyeColor.byName((String) args.get(0), DyeColor.WHITE);
                this.parentBlockEntity.networkUpdate(this.parentBlockEntity.getOrCreate());
                return true;
            }
            return false;
        });
    }
}
