package moth.boxxed.panels.network.handler;

import moth.boxxed.panels.network.packet.OpenPaintWheelPacket;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleOpenPaintWheel(OpenPaintWheelPacket openPaintWheelPacket, IPayloadContext context) {
        openPaintWheelPacket.handle((ClientPayloadContext) context);
    }
}
