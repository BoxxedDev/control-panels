package moth.boxxed.panels.network.handler;

import moth.boxxed.panels.network.packet.NameValidationPacket;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleNameValidation(NameValidationPacket nameValidationPacket, IPayloadContext context) {
        nameValidationPacket.handleClient((ClientPayloadContext) context);
    }
}
