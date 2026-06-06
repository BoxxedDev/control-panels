package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.network.ModulesNetwork;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record SavePanelModulesPacket(Map<String, Module.ModuleInfo> modules, BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SavePanelModulesPacket> TYPE = new CustomPacketPayload.Type<>(Dashpanels.path("control_panel_save"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavePanelModulesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, Module.ModuleInfo.STREAM_CODEC), SavePanelModulesPacket::modules,
            BlockPos.STREAM_CODEC, SavePanelModulesPacket::pos,
            SavePanelModulesPacket::new
    );

    @SuppressWarnings("all")
    public void handle(ServerPayloadContext context) {
        ServerPlayer player = context.player();
        Level level = player.level();
        BlockEntity be = level.getBlockEntity(this.pos);
        if (be instanceof PanelBlockEntity pbe) {
            pbe.clearModules();
            ModulesNetwork network = pbe.getOrCreate();
            for (Map.Entry<String, Module.ModuleInfo> entry : this.modules.entrySet()) {
                String name = network.validateName(entry.getKey(), entry.getValue());
                Module module = Objects.requireNonNull(entry.getValue().create());
                module.name = name;
                pbe.tryAddModule(module.name, module);
            }
            pbe.networkUpdate(network);

            pbe.setChanged();
            pbe.blockChanged();
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
