package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.network.ModulesNetwork;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record PlaceModulePacket(BlockPos pos, Module.ModuleInfo moduleInfo) implements CustomPacketPayload {
    public static final Type<PlaceModulePacket> TYPE = new Type<>(Dashpanels.path("place_module"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceModulePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PlaceModulePacket::pos,
            Module.ModuleInfo.STREAM_CODEC, PlaceModulePacket::moduleInfo,
            PlaceModulePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext context) {
        Level level = context.player().level();
        BlockEntity blockEntity = level.getBlockEntity(this.pos);
        if (blockEntity instanceof AbstractPanelBlockEntity pbe) {
            Module module = this.moduleInfo.create(level.registryAccess());
            if (module == null)
                return;
            ModulesNetwork network = pbe.getOrCreate();
            ItemStack inHandStack = context.player().getMainHandItem();
            String generatedName = network.validateName(null, this.moduleInfo);
            if (pbe.tryAddModule(generatedName, module) && !context.player().isCreative()) {
                inHandStack.shrink(1);
            }

            pbe.networkUpdate(network);

            pbe.reconstructItems();

            pbe.setChanged();
            pbe.blockChanged();
        }
    }
}
