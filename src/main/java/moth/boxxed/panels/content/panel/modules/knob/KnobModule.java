package moth.boxxed.panels.content.panel.modules.knob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.IInput;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelHoldInteractions;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;

public class KnobModule extends Module implements IExternalUpdatable, IInput {
    private float renderAngle = 0;
    private int angle = 0;

    public KnobModule(int x, int y) {
        super(PanelModules.KNOB.get(), x, y, 2, 2);
    }

    @Override
    public InteractionResult onUse(Level level, Player player) {
        if (!PanelHoldInteractions.KNOB.isActive()) {
            PanelHoldInteractions.KNOB.startHold(level, player, this);
            return InteractionResult.SUCCESS;
        }
        return super.onUse(level, player);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        this.renderAngle = Math.lerp(this.renderAngle, (float) this.angle, 0.75f);
    }

    @Override
    public VoxelShape getShape() {
        return Block.box(0, 0, 0, 2, 2, 2);
    }

    @Override
    public boolean saveData(CompoundTag tag) {
        tag.putInt("num", this.angle);
        tag.putFloat("render_angle", this.renderAngle);

        return super.saveData(tag);
    }

    @Override
    public boolean loadData(CompoundTag tag) {
        this.angle = tag.getInt("num");
        this.renderAngle = tag.getFloat("render_angle");

        return super.loadData(tag);
    }

    @Override
    public void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = panelBlockEntity.getLevel();
        BlockState state = panelBlockEntity.getBlockState();
        BlockPos pos = panelBlockEntity.getBlockPos();

        poseStack.pushPose();
        poseStack.rotateAround(Axis.YP.rotationDegrees(this.renderAngle-45), 1/16f, 0, 1/16f);
        PanelPreloadedModels.KNOB.render(level, state, pos, poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.popPose();
    }

    public int getAngle() {
        return this.angle;
    }

    @Override
    public void setNum(int num) {
        this.angle = num;
        float f = (angle+360)/360f;
        this.parentBlockEntity.getLevel().playSound(null, this.getParentPos(), SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.1f, f);
    }

    @Override
    public int getAnalog() {
        return Math.round(Mth.clampedMap(java.lang.Math.floorMod(this.angle, 360), 0, 360, 0, 15));
    }
}
