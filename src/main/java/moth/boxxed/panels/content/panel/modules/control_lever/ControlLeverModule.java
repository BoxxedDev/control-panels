package moth.boxxed.panels.content.panel.modules.control_lever;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.IInput;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelHoldInteractions;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

import java.util.List;
import java.util.function.BiConsumer;

public class ControlLeverModule extends Module implements IExternalUpdatable, IInput, IModuleLuaObject {
    private float lastRenderSignal = 0;
    private float lastIndicatorRender = 0;
    private float renderSignal = 0;
    private float indicatorRender = 0;
    private int signal = 0;

    public ControlLeverModule(int x, int y) {
        super(PanelModules.CONTROL_LEVER.get(), x, y, 3, 5);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("signal", this.signal);
        tag.putFloat("render_signal", this.renderSignal);
        tag.putFloat("indicator_render", this.indicatorRender);
        tag.putFloat("last_render_signal", this.lastRenderSignal);
        tag.putFloat("last_indicator_render", this.lastIndicatorRender);

        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.signal = tag.getInt("signal");
        this.renderSignal = tag.getFloat("render_signal");
        this.indicatorRender = tag.getFloat("indicator_render");
        this.lastRenderSignal = tag.getFloat("last_render_signal");
        this.lastIndicatorRender = tag.getFloat("last_indicator_render");

        return super.loadData(tag, registries);
    }

    @Override
    public InteractionResult onUse(Level level, Player player) {
        if (level.isClientSide && player.isLocalPlayer())
            if (!PanelHoldInteractions.CONTROL_LEVER.isActive()) {
                PanelHoldInteractions.CONTROL_LEVER.startHold(level, player, this);
                return InteractionResult.SUCCESS;
            }
        return super.onUse(level, player);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        this.lastRenderSignal = this.renderSignal;
        this.lastIndicatorRender = this.indicatorRender;
        this.renderSignal = Math.lerp(this.renderSignal, Mth.map(this.signal, 0, 15, 0, 0.25f), 0.5f);
        this.indicatorRender = Math.lerp(this.indicatorRender, Mth.map(this.signal, 0, 15, 0,0.25f), 0.15f);
    }

    @Override
    public VoxelShape getShape() {
        return Block.box(0, 0, 0, 3, 1, 5);
    }

    @Override
    public void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.CONTROL_LEVER_BASE.render(poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0, 0, Mth.lerp(partialTick, this.lastRenderSignal, this.renderSignal));
        PanelPreloadedModels.CONTROL_LEVER_HANDLE.render(poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0, 0, Mth.lerp(partialTick, this.lastIndicatorRender, this.indicatorRender));
        PanelPreloadedModels.CONTROL_LEVER_INDICATOR.render(poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.popPose();
    }

    @Override
    public void setNum(List<Integer> num) {
        this.signal = num.getFirst();
        float f = (this.signal+15)/15f;
        this.parentBlockEntity.getLevel().playSound(null, this.getParentPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, f);
    }

    public int getSignal() {
        return this.signal;
    }

    @Override
    public int getAnalog() {
        return this.signal;
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getValue", args -> this.getSignal());
        consumer.accept("setValue", args -> {
            if (args.count() != 1)
                return false;
            if (args.get(0) instanceof Number number) {
                this.signal = Math.clamp(0, 15, number.intValue());
                this.parentBlockEntity.networkUpdate(this.parentBlockEntity.getOrCreate());
                return true;
            }
            return false;
        });
    }
}
