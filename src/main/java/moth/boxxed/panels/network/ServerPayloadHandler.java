package moth.boxxed.panels.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public class ServerPayloadHandler {

    public static void handleSavePanelModules(SavePanelModulesPacket savePanelModulesPacket, IPayloadContext context) {
        savePanelModulesPacket.handle((ServerPayloadContext) context);
    }

    public static void handleDefaultUpdate(DefaultModuleUpdatePacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }
}
