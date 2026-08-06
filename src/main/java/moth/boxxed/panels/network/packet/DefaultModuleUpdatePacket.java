package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record DefaultModuleUpdatePacket(BlockPos pos, String moduleName, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<DefaultModuleUpdatePacket> TYPE = new Type<DefaultModuleUpdatePacket>(Dashpanels.path("update_module"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultModuleUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DefaultModuleUpdatePacket::pos,
            ByteBufCodecs.STRING_UTF8, DefaultModuleUpdatePacket::moduleName,
            ByteBufCodecs.fromCodec(CompoundTag.CODEC), DefaultModuleUpdatePacket::tag,
            DefaultModuleUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("all")
    public void handle(ServerPayloadContext context) {
        Level level = context.player().level();

        if (level.getBlockEntity(this.pos) instanceof AbstractPanelBlockEntity pbe) {
            if (pbe.getModule(this.moduleName) instanceof IExternalUpdatable updatable) {
                updatable.update(context.player(), this.tag, level.registryAccess());
                pbe.setChanged();
                pbe.blockChanged();
            }
        }
    }
}
