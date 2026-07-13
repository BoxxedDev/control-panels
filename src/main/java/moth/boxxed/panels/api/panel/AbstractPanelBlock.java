package moth.boxxed.panels.api.panel;

import moth.boxxed.panels.content.panel.normal.PanelBlock;
import moth.boxxed.panels.util.BaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPanelBlock extends BaseEntityBlock {
    public static final EnumProperty<PanelBlock.Shape> SHAPE = EnumProperty.create("shape", PanelBlock.Shape.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE);
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }

    public enum Shape implements StringRepresentable {
        SINGLE,
        LEFT,
        CENTER,
        RIGHT;

        @Override
        public String getSerializedName() {
            return this.toString().toLowerCase();
        }
    }
}
