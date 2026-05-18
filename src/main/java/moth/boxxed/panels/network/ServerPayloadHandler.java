package moth.boxxed.panels.network;

import moth.boxxed.panels.ControlPanels;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public class ServerPayloadHandler {

    public static void handleSavePanelModules(SavePanelModulesPacket savePanelModulesPacket, IPayloadContext context) {
        savePanelModulesPacket.handle((ServerPayloadContext) context);
    }
}
