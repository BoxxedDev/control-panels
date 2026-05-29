package moth.boxxed.panels.network.packet.name;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerBoundNamePacket() implements CustomPacketPayload {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }
}
