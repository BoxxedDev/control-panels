package moth.boxxed.panels.content.paintbrush;

import moth.boxxed.panels.api.panel.PanelSkinsServerManager;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.api.panel.ServerSkin;
import moth.boxxed.panels.network.packet.OpenPaintWheelPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;

public class PaintbrushItem extends Item {
    public PaintbrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer().isLocalPlayer()) return InteractionResult.PASS;
        BlockState clickedBlock = context.getLevel().getBlockState(context.getClickedPos());
        for (PanelType type : PanelType.values()) {
            if (clickedBlock.is(type.block)) {
                PacketDistributor.sendToPlayer((ServerPlayer) context.getPlayer(), new OpenPaintWheelPacket(PanelSkinsServerManager.MAP.computeIfAbsent(type, type1 -> new ServerSkin(type, new HashSet<>())), context.getClickedPos()));
            }
        }

        return super.useOn(context);
    }
}
