package moth.boxxed.panels.network.handler;

import moth.boxxed.panels.network.packet.DefaultModuleUpdatePacket;
import moth.boxxed.panels.network.packet.NameValidationPacket;
import moth.boxxed.panels.network.packet.SavePanelModulesPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public class ServerPayloadHandler {
    public static void handleSavePanelModules(SavePanelModulesPacket savePanelModulesPacket, IPayloadContext context) {
        savePanelModulesPacket.handle((ServerPayloadContext) context);
    }

    public static void handleDefaultUpdate(DefaultModuleUpdatePacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }

    public static void handleNameValidation(NameValidationPacket nameValidationPacket, IPayloadContext context) {
        nameValidationPacket.handleServer((ServerPayloadContext) context);
    }
}
