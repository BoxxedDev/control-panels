package moth.boxxed.panels.content.panel;

import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.content.cable.CableBlock;
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

        if (direction.equals(Direction.UP))
            return false;
        if ((fromDirection.getOpposite()==direction || direction==Direction.DOWN) && to.getBlock() instanceof CableBlock)
            return true;
        return (fromDirection.getClockWise()==direction || fromDirection.getCounterClockWise()==direction) &&
                to.getBlock() instanceof PanelBlock &&
                from.getValue(PanelBlock.FACING) == to.getValue(PanelBlock.FACING);
    }
}
