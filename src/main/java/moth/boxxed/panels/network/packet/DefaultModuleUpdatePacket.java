package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.List;

public record DefaultModuleUpdatePacket(BlockPos pos, String moduleName, List<Integer> values) implements CustomPacketPayload {
    public static final Type<DefaultModuleUpdatePacket> TYPE = new Type<DefaultModuleUpdatePacket>(Dashpanels.path("update_module"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultModuleUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DefaultModuleUpdatePacket::pos,
            ByteBufCodecs.STRING_UTF8, DefaultModuleUpdatePacket::moduleName,
            new StreamCodec<>() {
                @Override
                public List<Integer> decode(RegistryFriendlyByteBuf buffer) {
                    return buffer.readList(ByteBufCodecs.INT);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, List<Integer> value) {
                    buffer.writeCollection(value, ByteBufCodecs.INT);
                }
            }, DefaultModuleUpdatePacket::values,
            DefaultModuleUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext context) {
        Level level = context.player().level();

        if (level.getBlockEntity(this.pos) instanceof AbstractPanelBlockEntity pbe) {
            if (pbe.getModule(this.moduleName) instanceof IExternalUpdatable updatable) {
                updatable.setNum(this.values);
                pbe.setChanged();
                pbe.blockChanged();
            }
        }
    }
}
