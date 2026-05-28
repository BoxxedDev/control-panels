package moth.boxxed.panels.content.cable.stripped;

import moth.boxxed.panels.api.network.connecting_panels.ModulesNetworkMember;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class StrippedCableBlockEntity extends ModulesNetworkMember {
    public StrippedCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.STRIPPED_CABLE.get(), pos, blockState);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof StrippedCableBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());
        Direction fromDirection = from.getValue(PanelBlock.FACING);

        if (direction.getAxis().isVertical())
            return false;
        return to.getBlock() instanceof CableBlock && fromDirection.getOpposite().equals(direction);
    }
}
