package moth.boxxed.panels.content.modules.buzzer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;

public class BuzzerModule extends Module implements IOutput {
    private int power = 0;

    private final ModuleConfigValue.FloatRangeValue pitchRange = new ModuleConfigValue.FloatRangeValue(
            "pitch_range",
            1f, 2f,
            0.5f, 3f
    );
    private final ModuleConfigValue.IntValue threshold = new ModuleConfigValue.IntValue(
            "threshold",
            0, 0, 14
    );

    public BuzzerModule(int x, int y) {
        super(PanelModules.BUZZER.get(), x, y);
    }

    @Override
    public void createConfig(ModuleConfig.Builder builder) {
        builder.add(threshold);
        builder.add(pitchRange);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("power", this.power);
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.power = tag.getInt("power");
        return super.loadData(tag, registries);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.BUZZER.render(poseStack, packedLight);

        if (this.power > this.threshold.get()) {
            poseStack.pushPose();
            float scale = Mth.map(this.power, 0, 15, 0.1f, 0.5f);
            poseStack.scale(scale, scale, scale);
            float xz = (1/scale)*0.125f;
            float y = (1/scale)*0.125f;
            poseStack.translate(xz, y, xz);
            poseStack.mulPose(Axis.XP.rotation(45));
            PanelPreloadedModels.SOUND_INDICATOR.render(poseStack, packedLight);
            poseStack.popPose();
        }
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Block.box(0, 0, 0, 4, 1, 4);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 4, 4);
    }

    @Override
    public void setAnalog(int signal) {
        this.power = signal;
    }

    @Override
    public void onRemove(Player player) {
//        if (this.getLevel().isClientSide)
//            if (soundInstance != null) {
//                soundInstance.deactivate();
//                this.soundInstance = null;
//            }
    }

    @Override
    public boolean canRotate() {
        return false;
    }

    @Override
    public void onUnloaded() {
//        if (this.getLevel().isClientSide)
//            if (soundInstance != null) {
//                soundInstance.deactivate();
//                this.soundInstance = null;
//            }
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide) {
            this.tickClient();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private BuzzSoundInstance soundInstance;

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        if (this.power <= this.threshold.get()) {
            if (this.soundInstance != null) {
                this.soundInstance.deactivate();
                this.soundInstance = null;
            }
            return;
        }

        if (this.soundInstance == null || this.soundInstance.isStopped()) {
            Minecraft.getInstance().getSoundManager()
                    .play(this.soundInstance = new BuzzSoundInstance(this.getParentPos()));
        }
        this.soundInstance.resetLife();

        float p = Mth.map(this.power, this.threshold.get(), 15, this.pitchRange.get().getMinimum(), this.pitchRange.get().getMaximum());
        this.soundInstance.setPitch(p);
    }

    @Override
    public void buildComputerMethods(ModuleMethodBuilder builder) {
        builder.addReturn("buzzing", args -> this.power > this.threshold.get());
        builder.addVoid("setPower", args -> {
            if (args.count() != 1)
                throw new ModuleLuaException("Arg amount cannot be less than or greater than 1");
            if (args.get(0) instanceof Number number) {
                this.power = Math.clamp(number.intValue(), 0, 15);
            } else {
                throw new ModuleLuaException("First arg has to be an integer");
            }
        });
    }
}
