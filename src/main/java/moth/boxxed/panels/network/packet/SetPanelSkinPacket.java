package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.Optional;

public record SetPanelSkinPacket(BlockPos pos, ResourceLocation skin, Optional<Integer> skinColor) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetPanelSkinPacket> TYPE = new Type<>(Dashpanels.path("set_panel_skin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetPanelSkinPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetPanelSkinPacket::pos,
            ResourceLocation.STREAM_CODEC, SetPanelSkinPacket::skin,
            ByteBufCodecs.optional(ByteBufCodecs.INT), SetPanelSkinPacket::skinColor,
            SetPanelSkinPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext serverPayloadContext) {
        Level level = serverPayloadContext.player().level();
        if (level.getBlockEntity(this.pos) instanceof AbstractPanelBlockEntity pbe) {
            pbe.setSkin(this.skin);
            pbe.setSkinColor(this.skinColor.orElse(0xFFFFFFFF));
        }
    }
}
