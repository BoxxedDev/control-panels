package moth.boxxed.panels.content.cable;

import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends BlockEntity {
    public CableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.CABLE.get(), pos, blockState);
    }
}