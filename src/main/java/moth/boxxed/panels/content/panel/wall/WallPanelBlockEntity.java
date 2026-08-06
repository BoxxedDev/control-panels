package moth.boxxed.panels.content.panel.wall;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.util.function.BiConsumer;

public class WallPanelBlockEntity extends AbstractPanelBlockEntity {
    public WallPanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelType.WALL, PanelBlockEntities.WALL_PANEL.get(), pos, blockState);
    }

    @Override
    public void transformPanelClipping(PoseStack stack) {
        Direction direction = this.getBlockState().getValue(WallPanelBlock.FACING);
        stack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        stack.pushPose();
//        stack.rotateAround(Axis.ZP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        stack.rotateAround(Axis.XP.rotationDegrees(90), 0.5f, 0.5f, 0.5f);
        stack.translate(0, 0.125f, -0.25f);
        stack.translate(0, 0, 0.25f);
    }

    @Override
    public boolean canPlaceModuleOnSurface(Vec3 position, Direction face) {
        BlockState state = this.getBlockState();
        Direction blockDirection = state.getValue(WallPanelBlock.FACING);
        return face==blockDirection;
    }

    @Override
    public Vector2i getPosForModule(Vec3 localSpace) {
        return new Vector2i(
                (int) Math.round(Mth.map(localSpace.x, -0.5, 0.5, 0, 16)),
                (int) Math.round(Mth.map(localSpace.y, 0, 1, 16, 0))
        );
    }

    @Override
    public BiConsumer<Module, PoseStack> getIndividualModuleTransform() {
        return (module, stack) -> stack.translate(module.getPos().x/16f, 0, module.getPos().y/16f);
    }

    @Override
    public void renderTransform(PoseStack poseStack) {
//        poseStack.rotateAround(Axis.ZP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        poseStack.rotateAround(Axis.XP.rotationDegrees(90), 0.5f, 0.5f, 0.5f);
        poseStack.translate(0, 0.125f, 0);
    }

    @Override
    public Vector2i getContentArea() {
        return new Vector2i(16, 16);
    }
}
