package moth.boxxed.panels.api.panel;

import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.util.BaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPanelBlock extends BaseEntityBlock {
    public AbstractPanelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (this.getBlockEntity(level, pos) != null) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide)
                    this.openMenu(player, this.getBlockEntity(level, pos));
                return InteractionResult.SUCCESS;
            }
            InteractionResult result = this.getBlockEntity(level, pos).onUse(state, level, pos, player, hitResult);
            if (result != null)
                return result;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (this.getBlockEntity(level, pos) != null)
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide)
                    this.openMenu(player, this.getBlockEntity(level, pos));
                return ItemInteractionResult.SUCCESS;
            }

        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (this.getBlockEntity(level, pos) != null) {
            ItemInteractionResult result = this.getBlockEntity(level, pos).onItemUse(stack, state, level, pos, player, hitResult);
            if (result != null)
                return result;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public AbstractPanelBlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity blockEntity)
            return blockEntity;
        return null;
    }

    private void openMenu(Player player, AbstractPanelBlockEntity be) {
        ((ServerPlayer)player).openMenu(be, be::sendToMenu);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe) {
                if (level instanceof ServerLevel) {
                    Containers.dropContents(level, pos, pbe.container);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
