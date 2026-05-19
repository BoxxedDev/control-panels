package moth.boxxed.panels.network.connecting_panels;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectingPanelsNetwork {
    public static final List<ConnectingPanelsNetwork> ALL = new ArrayList<>();

    public Map<String, Module> collectiveModules;
    public List<BlockPos> members;

    //TODO: make a single method for both cables and panels maybe
    public ConnectingPanelsNetwork getOrCreateForPanel(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PanelBlockEntity pbe) {
            BlockState blockState = level.getBlockState(pos);
            Direction[] directions = {blockState.getValue(PanelBlock.FACING).getClockWise(), blockState.getValue(PanelBlock.FACING).getCounterClockWise()};
            for (Direction direction : directions) {
                BlockPos otherPos = pos.relative(direction);

            }
        }

        ConnectingPanelsNetwork ret = new ConnectingPanelsNetwork();
        ALL.add(ret);
        return ret;
    }
}
