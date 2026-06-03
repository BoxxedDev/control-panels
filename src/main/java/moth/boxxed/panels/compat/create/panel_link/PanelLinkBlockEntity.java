package moth.boxxed.panels.compat.create.panel_link;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlock;
import moth.boxxed.panels.content.panel.PanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PanelLinkBlockEntity extends ModulesNetworkMember {
    public PanelLinkBlockEntity(BlockPos pos, BlockState state) {
        super(PanelCreateRegistries.PANEL_LINK_BE.get(), pos, state);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof CableBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());

        return to.getBlock() instanceof CableBlock && (!direction.getAxis().isVertical());
    }
}
