package moth.boxxed.panels.content.cable;

import moth.boxxed.panels.content.panel.PanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CableBlock extends Block implements EntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private static final VoxelShape NORTH_SHAPE = Block.box(5, 0, 0, 11, 3, 5);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5, 0, 11, 11, 3, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(11, 0, 5, 16, 3, 11);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 5, 5, 3, 11);
    private static final VoxelShape CORE_SHAPE = Block.box(5, 0, 5, 11, 3, 11);

    private static final Map<Direction, BooleanProperty> directionPropertyMap = new HashMap<>();
    private static final Map<Direction, VoxelShape> directionShapeMap = new HashMap<>();
    static {
        directionPropertyMap.put(Direction.NORTH, CableBlock.NORTH);
        directionPropertyMap.put(Direction.SOUTH, CableBlock.SOUTH);
        directionPropertyMap.put(Direction.EAST, CableBlock.EAST);
        directionPropertyMap.put(Direction.WEST, CableBlock.WEST);

        directionShapeMap.put(Direction.NORTH, NORTH_SHAPE);
        directionShapeMap.put(Direction.SOUTH, SOUTH_SHAPE);
        directionShapeMap.put(Direction.EAST, EAST_SHAPE);
        directionShapeMap.put(Direction.WEST, WEST_SHAPE);
    }

    public CableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH);
        builder.add(EAST);
        builder.add(SOUTH);
        builder.add(WEST);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape output = CORE_SHAPE;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (state.getValue(directionPropertyMap.get(direction))) {
                output = Shapes.or(output, directionShapeMap.get(direction));
            }
        }
        return output;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState ret = this.defaultBlockState();

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = context.getClickedPos().relative(direction);
            BlockState neighborState = context.getLevel().getBlockState(neighborPos);

            boolean check1 = neighborState.getBlock() instanceof CableBlock && neighborState.getBlock() instanceof CableBlock;
            boolean check2 = neighborState.hasBlockEntity() && state.hasBlockEntity();
            boolean check3 = neighborState.getBlock() instanceof PanelBlock && neighborState.getValue(PanelBlock.FACING)==direction;

            ret = ret.setValue(directionPropertyMap.get(direction), (check1 && check2)||check3);
        }

        return ret;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos p3) {
        if (direction.getAxis().isVertical()) return state;

        boolean check1 = neighborState.getBlock() instanceof CableBlock && neighborState.getBlock() instanceof CableBlock;
        boolean check2 = neighborState.hasBlockEntity() && state.hasBlockEntity();
        boolean check3 = neighborState.getBlock() instanceof PanelBlock && neighborState.getValue(PanelBlock.FACING)==direction;

        return state.setValue(
                directionPropertyMap.get(direction),
                (check1 && check2) || check3
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }
}
