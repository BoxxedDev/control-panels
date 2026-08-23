package moth.boxxed.panels.content.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.io.IOutput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.compat.computercraft.ModuleLuaException;
import moth.boxxed.panels.compat.computercraft.ModuleMethodBuilder;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PolyVoxel;
import moth.boxxed.panels.util.PreLoadedModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiConsumer;

public class IndicatorBulbModule extends Module implements IOutput {
    public DyeColor color;
    public boolean lit;

    private final ModuleConfigValue.BooleanValue togglable = new ModuleConfigValue.BooleanValue("togglable", false);

    public IndicatorBulbModule(int x, int y) {
        super(PanelModules.INDICATOR_BULB.get(), x, y);
        this.color = DyeColor.WHITE;
        this.lit = false;
    }

    private int previousSignal = 0;

    @Override
    public void setAnalog(int signal) {
        if (this.togglable.get()) {
            if (previousSignal == 0 && signal != previousSignal) {
                this.lit = !this.lit;
            }
        } else {
            this.lit = signal > 0;
        }
        this.previousSignal = signal;
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PreLoadedModel bulbModel = this.lit ? PanelPreloadedModels.INDICATOR_BULB_ON : PanelPreloadedModels.INDICATOR_BULB_OFF;
        int bulbLight = this.lit ? LightTexture.FULL_BRIGHT : packedLight;

        poseStack.pushPose();
        poseStack.translate(0, 0, 0.5/16f);
        PanelPreloadedModels.INDICATOR_BULB_BASE.render(poseStack, RenderType.solid(), packedLight);
        bulbModel.render(poseStack, RenderType.translucent(), bulbLight, this.color.getTextureDiffuseColor());
        poseStack.popPose();
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.color = DyeColor.byId(tag.getInt("color"));
        this.lit = tag.getBoolean("lit");
        return super.loadData(tag, registries);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("color", this.color.getId());
        tag.putBoolean("lit", this.lit);
        return super.saveData(tag, registries);
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
    public VoxelShape getVoxelShape() {
        return Block.box(0, 0, 0.5, 1, 1.5, 1.5);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 1, 2);
    }

    @Override
    public void createConfig(ModuleConfig.Builder builder) {
        builder.add(togglable);
    }

    @Override
    public void buildComputerMethods(ModuleMethodBuilder builder) {
        builder.addReturn("getState", args -> this.lit);
        builder.addReturn("getColor", args -> this.color.getSerializedName());
        builder.addVoid("setState", args -> {
            if (args.count() != 1)
                throw new ModuleLuaException("Arg amount cannot be less than or greater than 1");
            if (args.get(0) instanceof Boolean bool) {
                this.lit = bool;
            } else {
                throw new ModuleLuaException("First arg has to be a boolean");
            }
        });
        builder.addVoid("setColor", args -> {
            if (args.count() != 1)
                throw new ModuleLuaException("Arg amount cannot be less than or greater than 1");
            if (args.get(0) instanceof String string) {
                this.color = DyeColor.byName(string, DyeColor.WHITE);
            } else {
                throw new ModuleLuaException("First arg has to be a string");
            }
        });
    }
}
