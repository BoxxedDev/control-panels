package moth.boxxed.panels.network.packet;

import moth.boxxed.panels.Dashpanels;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record SetPlayerSlotPacket(int slot, ItemStack stack, int count) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetPlayerSlotPacket> TYPE = new Type<>(Dashpanels.path("set_player_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetPlayerSlotPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SetPlayerSlotPacket::slot,
            ItemStack.OPTIONAL_STREAM_CODEC, SetPlayerSlotPacket::stack,
            ByteBufCodecs.INT, SetPlayerSlotPacket::count,
            SetPlayerSlotPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPayloadContext context) {
        ServerPlayer player = context.player();
        Inventory inventory = player.getInventory();

        if (this.stack == ItemStack.EMPTY) {
            inventory.removeItem(slot, this.count);
        } else {
            inventory.setItem(slot, new ItemStack(this.stack.getItem(), count));
        }
    }
}
