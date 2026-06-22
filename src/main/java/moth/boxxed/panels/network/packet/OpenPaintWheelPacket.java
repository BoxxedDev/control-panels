package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.ServerSkin;
import moth.boxxed.panels.content.paintbrush.PaintWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;

import java.util.List;

public record OpenPaintWheelPacket(ServerSkin serverSkin, BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenPaintWheelPacket> TYPE = new CustomPacketPayload.Type<OpenPaintWheelPacket>(Dashpanels.path("open_paint_wheel"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPaintWheelPacket> STREAM_CODEC = StreamCodec.composite(
            ServerSkin.STREAM_CODEC, OpenPaintWheelPacket::serverSkin,
            BlockPos.STREAM_CODEC, OpenPaintWheelPacket::pos,
            OpenPaintWheelPacket::new
    );



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ClientPayloadContext context) {
        PaintWheel.open(this.serverSkin, this.pos);
    }
}
