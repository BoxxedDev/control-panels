package moth.boxxed.panels.util;

import moth.boxxed.panels.ControlPanels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseBlockEntity extends BlockEntity {
    private boolean chunkUnloaded = false;
    private boolean init;

    public BaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!this.chunkUnloaded)
            this.remove();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.chunkUnloaded = true;
    }

    public void remove() {}

    public void init() {}

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (!init) {
            this.init();
            this.init = true;
        }
    }
}
