package moth.boxxed.panels.content.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleHitResult;
import moth.boxxed.panels.api.module.io.IInput;
import moth.boxxed.panels.api.module.io.IMultiInput;
import moth.boxxed.panels.api.module.io.IOutput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiConsumer;

//I'll finish this once I patch allowing a module to have inputs and outputs
public class EmergencyButtonModule extends Module implements IOutput, IInput {
    public boolean open = false;
    public boolean pressed = false;

    private float openTime = 0;
    private float previousOpenTime = 0;

    private int previousInputSignal;

    public EmergencyButtonModule(int x, int y) {
        super(PanelModules.EMERGENCY_BUTTON.get(), x, y);
        Dashpanels.LOGGER.debug("Created");
    }

    @Override
    public InteractionResult onUse(ModuleHitResult hitResult, Level level, Player player) {
        if (this.open) {
            if (this.pressed && player.isShiftKeyDown()) {
                this.pressed = false;
                return InteractionResult.SUCCESS;
            } else if (!this.pressed && !player.isShiftKeyDown()) {
                this.pressed = true;
                return InteractionResult.SUCCESS;
            }
        }

        return super.onUse(hitResult, level, player);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        this.previousOpenTime = this.openTime;
        this.openTime = Math.clamp(this.openTime + (this.open ? 0.1f : -0.1f), 0, 1);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.EMERGENCY_BUTTON_BASE.render(poseStack, packedLight);

        poseStack.pushPose();
        poseStack.rotateAround(Axis.XP.rotationDegrees(ease(Mth.lerp(partialTick, this.previousOpenTime, this.openTime))*75), 0, 0 ,0.25f);
        PanelPreloadedModels.EMERGENCY_BUTTON_COVER.render(poseStack, RenderType.translucent(), packedLight);
        poseStack.popPose();

        poseStack.pushPose();
        if (this.pressed) {
            poseStack.translate(0, -0.75/16f, 0);
        }
        PanelPreloadedModels.EMERGENCY_BUTTON_BUTTON.render(poseStack, packedLight);
        poseStack.popPose();
    }

    @Override
    public VoxelShape getVoxelShape() {
        return this.open ? Block.box(0.5, 0, 0.5, 3.5, 1, 3.5) : Block.box(0, 0, 0, 4, 2, 4);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 4, 4);
    }

    @Override
    public int getAnalog() {
        return this.pressed ? 15 : 0;
    }

    @Override
    public void setAnalog(int signal) {
        if (previousInputSignal == 0 && signal != previousInputSignal) {
            this.open = !this.open;
            if (!this.open) {
                this.pressed = false;
            }
        }
        this.previousInputSignal = signal;
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("open", this.open);
        tag.putBoolean("pressed", this.pressed);

        tag.putFloat("open_time", this.openTime);
        tag.putFloat("previous_open_time", this.previousOpenTime);
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.open = tag.getBoolean("open");
        this.pressed = tag.getBoolean("pressed");

        this.openTime = tag.getFloat("open_time");
        this.previousOpenTime = tag.getFloat("previous_open_time");
        return super.loadData(tag, registries);
    }

    private static float ease(float x) {
        final float c1 = 1.70158f;
        final float c3 = c1 + 1;

        return (float) (1 + c3 * Math.pow(x-1, 3) + c1 * Math.pow(x-1, 2));
    }
}
