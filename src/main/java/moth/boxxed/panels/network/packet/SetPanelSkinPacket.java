package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.*;

public record SetPanelSkinPacket(PanelType panelType, BlockPos pos, ResourceLocation skin, Optional<Integer> skinColor, boolean applyToAll) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetPanelSkinPacket> TYPE = new Type<>(Dashpanels.path("set_panel_skin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetPanelSkinPacket> STREAM_CODEC = StreamCodec.composite(
            PanelType.STREAM_CODEC, SetPanelSkinPacket::panelType,
            BlockPos.STREAM_CODEC, SetPanelSkinPacket::pos,
            ResourceLocation.STREAM_CODEC, SetPanelSkinPacket::skin,
            ByteBufCodecs.optional(ByteBufCodecs.INT), SetPanelSkinPacket::skinColor,
            ByteBufCodecs.BOOL, SetPanelSkinPacket::applyToAll,
            SetPanelSkinPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext serverPayloadContext) {
        Level level = serverPayloadContext.player().level();

        if (this.applyToAll) {
            this.applySkinToAll(level);
        } else {
            setSkinAtPos(level, this.pos);
        }
    }

    private void applySkinToAll(Level level) {
        Set<BlockPos> visitedPositions = new HashSet<>();
        Queue<BlockPos> posQueue = new ArrayDeque<>();

        posQueue.add(this.pos);
        while (!posQueue.isEmpty()) {
            BlockPos queuedPos = posQueue.poll();
            setSkinAtPos(level, queuedPos);

            for (Direction direction : Direction.values()) {
                BlockPos relative = queuedPos.relative(direction);
                if (!visitedPositions.contains(relative) && level.getBlockState(relative).is(this.panelType.block)) {
                    posQueue.add(relative);
                }
            }

            visitedPositions.add(queuedPos);
        }
    }

    private void setSkinAtPos(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(this.panelType.block) && level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe) {
            pbe.setSkin(this.skin);
            pbe.setSkinColor(this.skinColor.orElse(0xFFFFFFFF));
        }
    }
}
