package moth.boxxed.panels.content.panel.modules.momentary_switch;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.IInput;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;

import java.util.List;
import java.util.function.BiConsumer;

public class MomentarySwitchModule extends Module implements IModuleLuaObject, IInput, IExternalUpdatable {
    public boolean pressed;
    private float pressValue;

    public MomentarySwitchModule(int x, int y) {
        super(PanelModules.MOMENTARY_SWITCH.get(), x, y, 3, 3);
        this.pressed = false;
        this.pressValue = 0;
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("pressed", this.pressed);
        tag.putFloat("pressValue", this.pressValue);
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.pressed = tag.getBoolean("pressed");
        this.pressValue = tag.getFloat("pressValue");
        return super.loadData(tag, registries);
    }

    @Override
    public int getAnalog() {
        return this.pressed ? 15 : 0;
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        float target = this.pressed ? -0.5f/16f + 0.001f : 0f;
        pressValue = Math.lerp(pressValue, target, 0.5f);
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.MOMENTARY_SWITCH_BASE.render(
                poseStack,
                bufferSource,
                RenderType.solid(),
                packedLight
        );
        poseStack.pushPose();
        poseStack.translate(0, pressValue, 0);
        PanelPreloadedModels.MOMENTARY_SWITCH_BUTTON.render(
                poseStack,
                bufferSource,
                RenderType.solid(),
                packedLight
        );
        poseStack.popPose();
    }

    @Override
    public VoxelShape getShape() {
        return Block.box(0, 0, 0, 3, 1, 3);
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getState", args -> this.pressed);
    }

    @Override
    public void setNum(List<Integer> num) {
        this.pressed = num.getFirst()==1;
        if (this.pressed) {
            this.parentBlockEntity.getLevel().playSound(
                    null, this.getParentPos(), SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.1f, 1f
            );
        } else {
            this.parentBlockEntity.getLevel().playSound(
                    null, this.getParentPos(), SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.1f, 1f
            );
        }
    }

    @Override
    public InteractionResult onUse(Level level, Player player) {
        if (level.isClientSide && player.isLocalPlayer())
            if (!PanelHoldInteractions.MOMENTARY_SWITCH.isActive()) {
                PanelHoldInteractions.MOMENTARY_SWITCH.startHold(level, player, this);
                return InteractionResult.SUCCESS;
            }
        return super.onUse(level, player);
    }
}
