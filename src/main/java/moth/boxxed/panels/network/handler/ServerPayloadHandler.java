package moth.boxxed.panels.network.handler;

import moth.boxxed.panels.network.packet.*;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public class ServerPayloadHandler {
    public static void handleDefaultUpdate(DefaultModuleUpdatePacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }

    public static void handleStrippedConfig(ConfigureStrippedCablePacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }

    public static void handleSavePanelLink(PanelLinkSaveEntriesPacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }

    public static void handleSetPlayerSlot(SetPlayerSlotPacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }

    public static void handleSelectedModule(SelectedModulePacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }

    public static void handleSetPanelSkin(SetPanelSkinPacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }

    public static void handlePlaceModule(PlaceModulePacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }

    public static void handleConfigureModule(ConfigureModulePacket packet, IPayloadContext context) {
        packet.handle((ServerPayloadContext) context);
    }
}
