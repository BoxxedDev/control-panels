package moth.boxxed.panels.network.connecting_panels;

import moth.boxxed.panels.api.module.Module;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectingPanelsNetwork {
    public static final List<ConnectingPanelsNetwork> ALL = new ArrayList<>();

    public Map<String, Module> collectiveModules;
    public List<BlockPos> members;


}
