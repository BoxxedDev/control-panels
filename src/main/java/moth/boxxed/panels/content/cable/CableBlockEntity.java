package moth.boxxed.panels.content.cable;

import moth.boxxed.panels.api.network.connecting_panels.ModulesNetworkMember;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends ModulesNetworkMember {
    public CableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.CABLE.get(), pos, blockState);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof CableBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());

        if (direction.getAxis().isVertical())
            return false;
        if (to.getBlock() instanceof CableBlock)
            return true;
        return to.getBlock() instanceof PanelBlock && to.getValue(PanelBlock.FACING) == direction;
    }
}