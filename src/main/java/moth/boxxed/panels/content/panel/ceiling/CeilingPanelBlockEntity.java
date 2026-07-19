package moth.boxxed.panels.content.panel.ceiling;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.util.function.BiConsumer;

public class CeilingPanelBlockEntity extends AbstractPanelBlockEntity {
    public CeilingPanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelType.CEILING, PanelBlockEntities.CEILING_PANEL.get(), pos, blockState);
    }

    @Override
    public void transformPanelClipping(PoseStack stack) {

    }

    @Override
    public boolean canPlaceModuleOnSurface(Vec3 position, Direction face) {
        return false;
    }

    @Override
    public Vector2i getPosForModule(Vec3 localSpace) {
        return new Vector2i();
    }

    @Override
    public BiConsumer<Module, PoseStack> getIndividualModuleTransform() {
        return (module, stack) -> {};
    }

    @Override
    public void renderTransform(PoseStack poseStack) {

    }

    @Override
    public Vector2i getContentArea() {
        return new Vector2i(16, 16);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        return false;
    }
}
