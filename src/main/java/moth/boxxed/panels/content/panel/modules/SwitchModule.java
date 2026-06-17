package moth.boxxed.panels.content.panel.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.IInput;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
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

public class SwitchModule extends Module implements IInput, IModuleLuaObject {
    private boolean switchState;

    public SwitchModule(int x, int y) {
        super(PanelModules.SWITCH.get(),  x, y, 2,3);
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
        model.render(poseStack, bufferSource, RenderType.solid(), packedLight);
    }

    @Override
    public VoxelShape getShape() {
        return Shapes.or(
                Block.box(0, 0, 1, 2, 1, 3),
                Block.box(0.5, 0, 0, 1.5, 1, 1)
        );
    }

    @Override
    public int getAnalog() {
        return this.switchState ? 15 : 0;
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getState", args -> this.switchState);
        consumer.accept("setState", args -> {
            if (args.count() != 1)
                return false;
            if (args.get(0) instanceof Boolean bool) {
                this.switchState = bool;
                this.parentBlockEntity.networkUpdate(this.parentBlockEntity.getOrCreate());
                return true;
            }
            return false;
        });
    }
}
