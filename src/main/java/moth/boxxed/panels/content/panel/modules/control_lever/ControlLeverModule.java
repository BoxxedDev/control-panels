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
import net.minecraft.core.HolderGetter;
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

import java.util.function.BiConsumer;

public class ControlLeverModule extends Module implements IExternalUpdatable, IInput, IModuleLuaObject {
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

        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.signal = tag.getInt("signal");
        this.renderSignal = tag.getFloat("render_signal");
        this.indicatorRender = tag.getFloat("indicator_render");

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
        this.renderSignal = Math.lerp(this.renderSignal, Mth.map((float) this.signal, 0, 15, 0, 0.25f), 0.5f);
        this.indicatorRender = Math.lerp(this.indicatorRender, Mth.map((float) this.signal, 0, 15, 0,0.25f), 0.15f);
    }

    private static float bounce(float a, float b, float t) {
        final float c1 = 1.7f;
        final float c2 = c1 + 1;
        final float c3 = (float) (1f + c2 * java.lang.Math.pow(t-1, 3) + c1 * java.lang.Math.pow(t-1, 2));

        return Math.lerp(a, b, c3);
    }

    @Override
    public VoxelShape getShape() {
        return Block.box(0, 0, 0, 3, 1, 5);
    }

    @Override
    public void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = panelBlockEntity.getLevel();
        BlockState state = panelBlockEntity.getBlockState();
        BlockPos pos = panelBlockEntity.getBlockPos();

        PanelPreloadedModels.CONTROL_LEVER_BASE.render(level, state, poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0, 0, this.renderSignal);
        PanelPreloadedModels.CONTROL_LEVER_HANDLE.render(level, state, poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0, 0, this.indicatorRender);
        PanelPreloadedModels.CONTROL_LEVER_INDICATOR.render(level, state, poseStack, bufferSource, RenderType.solid(), packedLight);
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

    @Override
    public int getAnalog() {
        return this.signal;
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getValue", args -> this.getSignal());
    }
}
