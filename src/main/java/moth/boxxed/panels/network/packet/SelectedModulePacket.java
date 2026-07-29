package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.Optional;

public record SelectedModulePacket(String string, Optional<Vec3> location, BlockPos pos) implements CustomPacketPayload {
    public static final Type<SelectedModulePacket> TYPE = new CustomPacketPayload.Type<>(Dashpanels.path("selected_module"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectedModulePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SelectedModulePacket::string,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(Vec3.CODEC)), SelectedModulePacket::location,
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
        if (!(be instanceof AbstractPanelBlockEntity pbe)) return;
        pbe.setSelectedModule(context.player(), location.orElse(null), this.string);
    }
}
