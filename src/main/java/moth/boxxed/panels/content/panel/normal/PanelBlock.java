package moth.boxxed.panels.content.panel.normal;

import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.index.PanelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PanelBlock extends AbstractPanelBlock {
    public PanelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isConnecting(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        BlockPos otherPos = pos.relative(face);
        BlockState to = level.getBlockState(otherPos);
        Direction fromDirection = state.getValue(PanelBlock.FACING);

        if (to.getBlock() instanceof AbstractPanelBlock) {
            boolean facingCheck = to.getValue(AbstractPanelBlock.FACING) == fromDirection;
            boolean sideCheck = (fromDirection.getClockWise() == face || fromDirection.getCounterClockWise() == face) &&
                    to.getBlock() instanceof PanelBlock;
            boolean aboveCheck = face == Direction.UP && (!(to.getBlock() instanceof PanelBlock));

            return facingCheck && (sideCheck || aboveCheck);
        }

        return face == fromDirection.getOpposite() || face == Direction.DOWN;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PanelShapes.PANEL_SHAPE.get(state.getValue(FACING));
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
}
