package moth.boxxed.panels.network.packet;

import com.mojang.datafixers.util.Pair;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.util.EnumStreamCodec;
import moth.boxxed.panels.util.StreamCodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.io.ObjectInputFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record ConfigureModulePacket(BlockPos pos, Optional<BlockPos> newParentPos, String moduleName, Pair<Integer, Integer> modulePos, Module.Rotation moduleRotation, Map<String, CompoundTag> configValues) implements CustomPacketPayload {
    public static final Type<ConfigureModulePacket> TYPE = new Type<>(Dashpanels.path("configure_module_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureModulePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConfigureModulePacket::pos,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), ConfigureModulePacket::newParentPos,
            ByteBufCodecs.STRING_UTF8, ConfigureModulePacket::moduleName,
            StreamCodecUtil.pair(ByteBufCodecs.INT, ByteBufCodecs.INT), ConfigureModulePacket::modulePos,
            Module.Rotation.STREAM_CODEC, ConfigureModulePacket::moduleRotation,
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

            AbstractPanelBlockEntity otherPbe = null;
            if (this.newParentPos.isPresent() && level.getBlockEntity(newParentPos.get()) instanceof AbstractPanelBlockEntity) {
                otherPbe = (AbstractPanelBlockEntity) level.getBlockEntity(newParentPos.get());
                module = pbe.removeModule(moduleName);
                otherPbe.addModule(moduleName, module);
            } else {
                module.setParentBE(pbe);
            }

            module.setPos(this.modulePos.getFirst(), this.modulePos.getSecond());
            module.setRotation(this.moduleRotation);
            RegistryAccess registryAccess = level.registryAccess();
            for (ModuleConfigValue<?, ?> value : module.getConfig().getValues()) {
                CompoundTag valueTag = this.configValues.get(value.getId());
                if (valueTag == null)
                    continue;

                value.loadAndBroadcastChange(valueTag, registryAccess);

                if (value.getId() == "name") {
                    module.parentBlockEntity.renameModule(module.getName(), (String) value.get());
                    continue;
                }
            }

            pbe.setChanged();
            pbe.blockChanged();

            if (otherPbe != null) {
                otherPbe.setChanged();
                otherPbe.blockChanged();
            }
        }
    }
}
