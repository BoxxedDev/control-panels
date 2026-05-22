package moth.boxxed.panels.network.connecting_panels;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import net.minecraft.core.Direction;

import java.util.Map;

public interface IConnectingModules {
    ModuleMap getModules();
    boolean isConnecting(Direction direction);
    ConnectingModulesNetwork getNetwork();
    void setNetwork(ConnectingModulesNetwork network);
}
