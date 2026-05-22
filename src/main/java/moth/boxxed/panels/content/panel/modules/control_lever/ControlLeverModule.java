package moth.boxxed.panels.content.panel.modules.control_lever;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.IExternalUpdatable;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;

public class ControlLeverModule extends Module implements IExternalUpdatable {
    private float renderSignal = 0;
    private int signal = 0;

    public ControlLeverModule(int x, int y) {
        super(PanelModules.CONTROL_LEVER.get(), x, y, 4, 5);
    }

    @Override
    public boolean saveData(CompoundTag tag) {
        tag.putInt("signal", this.signal);
        tag.putFloat("render_signal", this.renderSignal);

        return super.saveData(tag);
    }

    @Override
    public boolean loadData(CompoundTag tag) {
        this.signal = tag.getInt("signal");
        this.renderSignal = tag.getFloat("render_signal");

        return super.loadData(tag);
    }

    @Override
    public InteractionResult onUse(Level level, Player player) {
        if (!PanelHoldInteractions.CONTROL_LEVER.isActive()) {
            PanelHoldInteractions.CONTROL_LEVER.startHold(level, player, this);
            return InteractionResult.SUCCESS;
        }
        return super.onUse(level, player);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        this.renderSignal = Math.lerp(this.renderSignal, ((float) this.signal)*0.0125f, 0.75f);
    }

    @Override
    public VoxelShape getShape() {
        return Block.box(0, 0, 0, 3, 0.5, 5);
    }

    @Override
    public void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = panelBlockEntity.getLevel();
        BlockState state = panelBlockEntity.getBlockState();
        BlockPos pos = panelBlockEntity.getBlockPos();

        poseStack.pushPose();
        poseStack.translate(0.03125, 0, 0);
        PanelPreloadedModels.CONTROL_LEVER_BASE.render(level, state, pos, poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0.09375,0,0.0625+this.renderSignal);
        PanelPreloadedModels.CONTROL_LEVER.render(level, state, pos, poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public void setNum(int num) {
        this.signal = num;
        float f = (this.signal+15)/15f;
        this.parentBlockEntity.getLevel().playSound(null, this.getParentPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, f);
    }

    public int getSignal() {
        return this.signal;
    }
}
