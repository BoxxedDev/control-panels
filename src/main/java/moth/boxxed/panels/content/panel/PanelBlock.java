package moth.boxxed.panels.content.panel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PanelBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape MAIN_SHAPE = Block.box(0,0,0,16,12,16);
    private static final VoxelShape TOP_N_SHAPE = Block.box(0, 12, 12, 16, 14, 16);
    private static final VoxelShape TOP_S_SHAPE = Block.box(0, 12, 0, 16, 14, 4);
    private static final VoxelShape TOP_E_SHAPE = Block.box(0, 12, 0, 4, 14, 16);
    private static final VoxelShape TOP_W_SHAPE = Block.box(12, 12, 0, 16, 14, 16);

    public PanelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (this.getBlockEntity(level, pos) != null) {
            if (player.isShiftKeyDown() && !level.isClientSide()) {
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
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        this.getBlockEntity(level, pos).getNetwork().removeMember(pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(
                MAIN_SHAPE,
                switch (state.getValue(FACING)) {
                    case SOUTH -> TOP_S_SHAPE;
                    case EAST -> TOP_E_SHAPE;
                    case WEST -> TOP_W_SHAPE;
                    default -> TOP_N_SHAPE;
                }
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PanelBlockEntity(blockPos, blockState);
    }

    public PanelBlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PanelBlockEntity blockEntity)
            return blockEntity;
        return null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level1, blockPos, blockState, t) -> ((PanelBlockEntity)t).tick(level1, blockPos, blockState);
    }

    private void openMenu(Player player, PanelBlockEntity be) {
        ((ServerPlayer)player).openMenu(be, be::sendToMenu);
    }
}
