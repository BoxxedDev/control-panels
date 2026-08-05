package moth.boxxed.panels.content.cable.stripper;

import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.cable.CableBlockEntity;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlock;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlockEntity;
import moth.boxxed.panels.content.panel.normal.PanelBlockEntity;
import moth.boxxed.panels.index.PanelBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CableStripperItem extends Item {
    public CableStripperItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.PASS;
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isCrouching()) {
            if (clickedState.getBlock() instanceof CableBlock || clickedState.getBlock() instanceof StrippedCableBlock) {
                level.destroyBlock(clickedPos, false);
                if (!player.isCreative()) {
                    ItemStack stack = new ItemStack(PanelBlocks.CABLE.asItem(), 1);
                    player.getInventory().add(stack);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (clickedState.getBlock() instanceof CableBlock) {
            Direction replaceDir = context.getHorizontalDirection().getOpposite();

            List<Direction> directions = new ArrayList<>();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (clickedState.getValue(CableBlock.directionPropertyMap.get(direction))) {
                    directions.add(direction.getOpposite());
                }
            }
            if (!directions.isEmpty()) {
                replaceDir = directions.getFirst();
            }
            BlockState strippedState = PanelBlocks.STRIPPED_CABLE.get().defaultBlockState()
                    .setValue(StrippedCableBlock.FACING, replaceDir);
            level.setBlockAndUpdate(clickedPos, strippedState);
            BlockEntity be = level.getBlockEntity(clickedPos.relative(replaceDir.getOpposite()));
            if (be instanceof CableBlockEntity cableBE) {
                UUID network = cableBE.network;
                be = level.getBlockEntity(clickedPos);
                if (be instanceof StrippedCableBlockEntity strippedBE) strippedBE.setNetwork(network);
            }
            if (be instanceof PanelBlockEntity panelBE) {
                UUID network = panelBE.network;
                be = level.getBlockEntity(clickedPos);
                if (be instanceof StrippedCableBlockEntity strippedBE) strippedBE.setNetwork(network);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

//    @Override
//    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
//        if (!tooltipFlag.hasShiftDown()) {
//            tooltipComponents.add(Component.translatable("tooltip.dashpanels.shift_to_expand"));
//        } else {
//            tooltipComponents.add(Component.translatable("tooltip.dashpanels.cable_stripper_info_1"));
//            tooltipComponents.add(Component.empty());
//            tooltipComponents.add(Component.translatable("tooltip.dashpanels.cable_stripper_info_2"));
//            tooltipComponents.add(Component.empty());
//            tooltipComponents.add(Component.translatable("tooltip.dashpanels.cable_stripper_info_3"));
//        }
//    }
}
