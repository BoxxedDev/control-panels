package moth.boxxed.panels.content.cable;

import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlock;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlock;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

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

        if (ModList.get().isLoaded("create"))
            if (to.getBlock() instanceof PanelLinkBlock)
                return true;
        if (to.getBlock() instanceof CableBlock)
            return true;
        boolean isPanel = to.getBlock() instanceof PanelBlock && to.getValue(PanelBlock.FACING) == direction;
        boolean isStripped = to.getBlock() instanceof StrippedCableBlock && to.getValue(StrippedCableBlock.FACING) == direction;
        return isStripped || isPanel;
    }
}