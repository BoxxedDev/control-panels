package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record DefaultModuleUpdatePacket(BlockPos pos, String moduleName, int num) implements CustomPacketPayload {
    public static final Type<DefaultModuleUpdatePacket> TYPE = new Type<DefaultModuleUpdatePacket>(ControlPanels.path("update_knob_module"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultModuleUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DefaultModuleUpdatePacket::pos,
            ByteBufCodecs.STRING_UTF8, DefaultModuleUpdatePacket::moduleName,
            ByteBufCodecs.INT, DefaultModuleUpdatePacket::num,
            DefaultModuleUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext context) {
        Level level = context.player().level();

        if (level.getBlockEntity(this.pos) instanceof PanelBlockEntity pbe) {
            if (pbe.getModule(this.moduleName) instanceof IExternalUpdatable updatable) {
                updatable.setNum(this.num);
                pbe.setChanged();
                pbe.blockChanged();
            }
        }
    }
}
