package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record SelectedModulePacket(String string, BlockPos pos) implements CustomPacketPayload {
    public static final Type<SelectedModulePacket> TYPE = new CustomPacketPayload.Type<>(Dashpanels.path("selected_module"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectedModulePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SelectedModulePacket::string,
            BlockPos.STREAM_CODEC, SelectedModulePacket::pos,
            SelectedModulePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext context) {
        Level level = context.player().level();
        BlockEntity be = level.getBlockEntity(this.pos);
        if (!(be instanceof PanelBlockEntity pbe)) return;
        pbe.setSelectedModule(this.string);
    }
}
