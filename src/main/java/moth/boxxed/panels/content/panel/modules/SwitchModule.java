package moth.boxxed.panels.content.panel.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.IInput;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PreLoadedModel;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;

public class SwitchModule extends Module implements IInput, IModuleLuaObject {
    private boolean switchState;

    public SwitchModule(int x, int y) {
        super(PanelModules.SWITCH.get(),  x, y, 2,3);
        this.switchState = false;
    }

    @Override
    public InteractionResult onUse(Level level, Player player) {
        this.switchState = !this.switchState;
        this.parentBlockEntity.getLevel().playSound(null, this.getParentPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, this.switchState ? 0.6f : 0.5f);

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean saveData(CompoundTag tag) {
        tag.putBoolean("state", this.switchState);
        return super.saveData(tag);
    }

    @Override
    public boolean loadData(CompoundTag tag) {
        this.switchState = tag.getBoolean("state");
        return super.loadData(tag);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PreLoadedModel model = this.switchState ? PanelPreloadedModels.SWITCH_ON : PanelPreloadedModels.SWITCH_OFF;

        Level level = panelBlockEntity.getLevel();
        BlockState state = panelBlockEntity.getBlockState();
        BlockPos pos = panelBlockEntity.getBlockPos();

        model.render(level, state, pos, poseStack, bufferSource, RenderType.solid(), packedLight);
    }

    @Override
    public VoxelShape getShape() {
        return Block.box(0, 0, 0, 2, 1, 3);
    }

    @Override
    public int getAnalog() {
        return this.switchState ? 15 : 0;
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getState", args -> this.switchState);
    }
}
