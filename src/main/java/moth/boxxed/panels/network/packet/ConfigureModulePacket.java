package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record ConfigureModulePacket(BlockPos pos, String moduleName, Map<String, CompoundTag> configValues) implements CustomPacketPayload {
    public static final Type<ConfigureModulePacket> TYPE = new Type<>(Dashpanels.path("configure_module_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureModulePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConfigureModulePacket::pos,
            ByteBufCodecs.STRING_UTF8, ConfigureModulePacket::moduleName,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.fromCodec(CompoundTag.CODEC)), ConfigureModulePacket::configValues,
            ConfigureModulePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("all")
    public void handle(ServerPayloadContext context) {
        Level level = context.player().level();
        if (level.getBlockEntity(this.pos) instanceof AbstractPanelBlockEntity pbe) {
            Module module = pbe.getModule(moduleName);
            if (module == null)
                return;

            module.setParentBE(pbe);
            RegistryAccess registryAccess = level.registryAccess();
            for (ModuleConfigValue<?, ?> value : module.getConfig().getValues()) {
                CompoundTag valueTag = this.configValues.get(value.getId());
                if (valueTag == null)
                    continue;

                value.loadAndBroadcastChange(valueTag, registryAccess);

                if (value.getId() == "name") {
                    pbe.renameModule(module.getName(), (String) value.get());
                    continue;
                }
            }

            pbe.setChanged();
            pbe.blockChanged();
        }
    }
}
