package moth.boxxed.panels.content.panel.normal;

import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PanelBlock extends AbstractPanelBlock {
    private static final VoxelShape MAIN_SHAPE = Block.box(0,0,0,16,12,16);
    private static final VoxelShape TOP_N_SHAPE = Block.box(0, 12, 12, 16, 16, 16);
    private static final VoxelShape TOP_S_SHAPE = Block.box(0, 12, 0, 16, 16, 4);
    private static final VoxelShape TOP_E_SHAPE = Block.box(0, 12, 0, 4, 16, 16);
    private static final VoxelShape TOP_W_SHAPE = Block.box(12, 12, 0, 16, 16, 16);

    public PanelBlock(Properties properties) {
        super(properties);
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
        Level level = context.getLevel();
        Direction placeDir = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();

        BlockState leftState = level.getBlockState(pos.relative(placeDir.getClockWise()));
        BlockState rightState = level.getBlockState(pos.relative(placeDir.getCounterClockWise()));

        Shape shape = Shape.SINGLE;
        boolean left = leftState.getBlock() instanceof PanelBlock && leftState.getValue(FACING) == placeDir;
        boolean right = rightState.getBlock() instanceof PanelBlock && rightState.getValue(FACING) == placeDir;
        if (left && !right)
            shape = Shape.RIGHT;
        if (!left && right)
            shape = Shape.LEFT;
        if (left && right)
            shape = Shape.CENTER;

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(SHAPE, shape);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction placeDir = state.getValue(FACING);

        BlockState leftState = level.getBlockState(pos.relative(placeDir.getClockWise()));
        BlockState rightState = level.getBlockState(pos.relative(placeDir.getCounterClockWise()));

        Shape shape = Shape.SINGLE;
        boolean left = leftState.getBlock() instanceof PanelBlock && leftState.getValue(FACING) == placeDir;
        boolean right = rightState.getBlock() instanceof PanelBlock && rightState.getValue(FACING) == placeDir;
        if (left && !right)
            shape = Shape.RIGHT;
        if (!left && right)
            shape = Shape.LEFT;
        if (left && right)
            shape = Shape.CENTER;

        return state.setValue(SHAPE, shape);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PanelBlockEntity(blockPos, blockState);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        super.spawnDestroyParticles(level, player, pos, state);
    }
}
