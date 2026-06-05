package moth.boxxed.panels.network.packet.name;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.network.packet.DefaultModuleUpdatePacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;

public record ClientBoundNamePacket(String name, NameType nameType) implements CustomPacketPayload {
    public static final Type<DefaultModuleUpdatePacket> TYPE = new Type<DefaultModuleUpdatePacket>(Dashpanels.path("update_knob_module"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundNamePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientBoundNamePacket::name,
            NameType.STREAM_CODEC, ClientBoundNamePacket::nameType,
            ClientBoundNamePacket::new
    );

    public void handle(ClientPayloadContext context) {

    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }
}
