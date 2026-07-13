package moth.boxxed.panels.content.panel.wall;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class WallPanelBlockEntity extends AbstractPanelBlockEntity {
    public WallPanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelType.WALL, PanelBlockEntities.WALL_PANEL.get(), pos, blockState);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof WallPanelBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());
        Direction fromDirection = from.getValue(WallPanelBlock.FACING);

        if (direction.equals(Direction.UP))
            return false;
        if ((fromDirection.getOpposite()==direction || direction==Direction.DOWN) && to.getBlock() instanceof CableBlock)
            return true;
        return (fromDirection.getClockWise()==direction || fromDirection.getCounterClockWise()==direction) &&
                to.getBlock() instanceof WallPanelBlock &&
                from.getValue(WallPanelBlock.FACING) == to.getValue(WallPanelBlock.FACING);
    }

    @Override
    public void transformPanelClipping(PoseStack stack) {
        Direction direction = this.getBlockState().getValue(WallPanelBlock.FACING);
        stack.translate(0, 0.75f, 0);
        stack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        stack.translate(0, 0, 0.25f);
    }
}
