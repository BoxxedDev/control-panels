package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.io.IOEntry;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record ConfigureStrippedCablePacket(IOEntry entry, BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigureStrippedCablePacket> TYPE = new CustomPacketPayload.Type<>(Dashpanels.path("configure_stripped"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureStrippedCablePacket> STREAM_CODEC = StreamCodec.composite(
            IOEntry.STREAM_CODEC, ConfigureStrippedCablePacket::entry,
            BlockPos.STREAM_CODEC, ConfigureStrippedCablePacket::pos,
            ConfigureStrippedCablePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext context) {
        Level level = context.player().level();
        if (level.getBlockEntity(this.pos) instanceof StrippedCableBlockEntity sbe) {
            sbe.setConfig(this.entry);
        }
    }
}
