package moth.boxxed.panels.content.cable.stripped;

import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StrippedCableBlockEntity extends BlockEntity {
    public StrippedCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.STRIPPED_CABLE.get(), pos, blockState);
    }
}
