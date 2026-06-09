package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.compat.create.panel_link.ModuleLinkEntries;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record PanelLinkSaveEntriesPacket(Map<String, ModuleLinkEntries.ModuleEntry> entries, BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PanelLinkSaveEntriesPacket> TYPE = new CustomPacketPayload.Type<>(Dashpanels.path("send_entries_to_panel_link"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PanelLinkSaveEntriesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ModuleLinkEntries.ModuleEntry.STREAM_CODEC), PanelLinkSaveEntriesPacket::entries,
            BlockPos.STREAM_CODEC, PanelLinkSaveEntriesPacket::pos,
            PanelLinkSaveEntriesPacket::new);

    public PanelLinkSaveEntriesPacket(ModuleLinkEntries entries, BlockPos pos) {
        this(entries.getMap(), pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext context) {
        Level level = context.player().level();

        BlockEntity be = level.getBlockEntity(this.pos);
        if (be instanceof PanelLinkBlockEntity panelLinkBlockEntity) {
            for (ModuleLinkEntries.ModuleEntry entry : this.entries.values()) {
                entry.setPos(this.pos);
                entry.setBe(panelLinkBlockEntity);
            }

            panelLinkBlockEntity.getModuleEntries().clearAll();
            panelLinkBlockEntity.getModuleEntries().clearFromNetworks(level);
            panelLinkBlockEntity.getModuleEntries().addAll(this.entries);
            panelLinkBlockEntity.networkUpdate(panelLinkBlockEntity.getOrCreate());
        }
    }
}
