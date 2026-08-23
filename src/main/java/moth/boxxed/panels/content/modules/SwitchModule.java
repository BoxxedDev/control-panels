package moth.boxxed.panels.content.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.io.IInput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.compat.computercraft.ModuleLuaException;
import moth.boxxed.panels.compat.computercraft.ModuleMethodBuilder;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PolyVoxel;
import moth.boxxed.panels.util.PreLoadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;

public class SwitchModule extends Module implements IInput {
    private final ModuleConfigValue.IntValue redstoneOutput = new ModuleConfigValue.IntValue("output", 15, 0, 15);
    private final ModuleConfigValue.BooleanValue inverted = new ModuleConfigValue.BooleanValue("inverted", false);

    private boolean switchState;

    public SwitchModule(int x, int y) {
        super(PanelModules.SWITCH.get(),  x, y);
        this.switchState = false;
    }

    @Override
    public InteractionResult onUse(Level level, Player player) {
        if (!level.isClientSide) {
            this.switchState = !this.switchState;
            this.parentBlockEntity.getLevel().playSound(null, this.getParentPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, this.switchState ? 0.6f : 0.5f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("state", this.switchState);
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.switchState = tag.getBoolean("state");
        return super.loadData(tag, registries);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PreLoadedModel model = this.switchState ? PanelPreloadedModels.SWITCH_ON : PanelPreloadedModels.SWITCH_OFF;
        model.render(poseStack, RenderType.solid(), packedLight);
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Shapes.or(
                Block.box(0, 0, 0, 2, 1, 2),
                Block.box(0.5, 0, 1, 1.5, 1, 3)
        );
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 2, 3);
    }

    @Override
    public int getAnalog() {
        int outputValue = this.redstoneOutput.get();
        boolean state = this.inverted.get() != this.switchState;
        return state ? outputValue : 0;
    }

    @Override
    public void createConfig(ModuleConfig.Builder builder) {
        builder.add(redstoneOutput);
        builder.add(inverted);
    }

    @Override
    public void buildComputerMethods(ModuleMethodBuilder builder) {
        builder.addReturn("getState", args -> this.switchState);
        builder.addVoid("setState", args -> {
            if (args.count() != 1)
                throw new ModuleLuaException("Arg amount cannot be less than or greater than 1");
            if (args.get(0) instanceof Boolean bool) {
                this.switchState = bool;
            } else {
                throw new ModuleLuaException("First arg has to be a boolean");
            }
        });
        builder.addVoid("flip", args -> this.switchState = !this.switchState);
    }
}
