package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.UUID;

/**
 * Packet used to generate or validate a network name on the client side
 * <br>
 * <br>
 * Packet sending sequence: Client -> Server -> Client
 * @param toName
 * @param networkId
 * @param returnName
 */
public record NameValidationPacket(ResourceLocation moduleType, String toName, UUID networkId, String returnName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NameValidationPacket> TYPE = new CustomPacketPayload.Type<>(Dashpanels.path("module_name_validation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NameValidationPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, NameValidationPacket::moduleType,
            ByteBufCodecs.STRING_UTF8, NameValidationPacket::toName,
            UUIDUtil.STREAM_CODEC, NameValidationPacket::networkId,
            ByteBufCodecs.STRING_UTF8, NameValidationPacket::returnName,
            NameValidationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Expects to only use {@link moth.boxxed.panels.network.packet.NameValidationPacket#toName}, {@link moth.boxxed.panels.network.packet.NameValidationPacket#networkId}, and {@link moth.boxxed.panels.network.packet.NameValidationPacket#moduleType} and then send another packet back to the client with returnName while
     * toName and the network id are empty
     * @param context
     */
    public void handleServer(ServerPayloadContext context) {
        Level level = context.player().level();
        ModuleType<?> type = ModulesRegistry.MODULE_REGISTRY.get(this.moduleType);
    }

    /**
     * Expects to only use {@link moth.boxxed.panels.network.packet.NameValidationPacket#returnName} and will not send another packet back
     * @param context
     */
    public void handleClient(ClientPayloadContext context) {

    }
}
