package moth.boxxed.panels.api.network;

import moth.boxxed.panels.util.BaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ModulesNetworkMemberBlock extends BaseEntityBlock {
    public ModulesNetworkMemberBlock(Properties properties) {
        super(properties);
    }

    public abstract boolean isConnecting(LevelReader level, BlockPos pos, BlockState state, Direction face);
}
