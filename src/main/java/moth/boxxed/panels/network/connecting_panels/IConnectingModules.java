package moth.boxxed.panels.network.connecting_panels;

import moth.boxxed.panels.api.module.Module;
import net.minecraft.core.Direction;

import java.util.Map;

public interface IConnectingModules {
    Map<String, Module> getModules();
    boolean isConnecting(Direction direction);
    ConnectingModulesNetwork getNetwork();
    void setNetwork(ConnectingModulesNetwork network);
}
