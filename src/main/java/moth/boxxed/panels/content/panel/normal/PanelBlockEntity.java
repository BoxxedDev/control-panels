package moth.boxxed.panels.content.panel.normal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.panel.wall.WallPanelBlock;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class PanelBlockEntity extends AbstractPanelBlockEntity {
    public PanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelType.DEFAULT, PanelBlockEntities.PANEL.get(), pos, blockState);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof PanelBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());
        Direction fromDirection = from.getValue(PanelBlock.FACING);

        if (direction.equals(Direction.UP) && to.getBlock() instanceof WallPanelBlock && to.getValue(WallPanelBlock.FACING) == fromDirection)
            return false;
        if ((fromDirection.getOpposite()==direction || direction==Direction.DOWN) && to.getBlock() instanceof CableBlock)
            return true;
        return (fromDirection.getClockWise()==direction || fromDirection.getCounterClockWise()==direction) &&
                to.getBlock() instanceof PanelBlock &&
                from.getValue(PanelBlock.FACING) == to.getValue(PanelBlock.FACING);
    }

    @Override
    public void transformPanelClipping(PoseStack stack) {
        Direction direction = this.getBlockState().getValue(PanelBlock.FACING);
        stack.translate(0, 0.75f, 0);
        stack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        stack.translate(0, 0, 0.25f);
    }
}
