package moth.boxxed.panels.network.connecting_panels;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ConnectingPanelsNetworkManager {
    public static final List<ConnectingPanelsNetwork> ALL = new ArrayList<>();

    //TODO: make a single method for both cables and panels maybe
    public static ConnectingPanelsNetwork getOrCreate(BlockEntity be) {
        if (!(be instanceof ConnectedPanelChild))
            return null;

        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();

        return null;
    }
}
