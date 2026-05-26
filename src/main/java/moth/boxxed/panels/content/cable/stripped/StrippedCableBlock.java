package moth.boxxed.panels.content.cable.stripped;

import moth.boxxed.panels.api.network.connecting_panels.ConnectingModulesNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class StrippedCableBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(4, 0, 0, 12, 0.01, 7),
            Block.box(6, 0, 7, 10, 1, 11),
            Block.box(6, 1, 9, 10, 2, 11),
            Block.box(5, 0, 11, 11, 3, 16)
    );
    public static final VoxelShape SOUTH_SHAPE = Shapes.or(
            Block.box(4, 0, 9, 12, 0.01, 16),
            Block.box(6, 0, 5, 10, 1, 9),
            Block.box(6, 1, 5, 10, 2, 7),
            Block.box(5, 0, 0, 11, 3, 5)
    );
    public static final VoxelShape WEST_SHAPE = Shapes.or(
            Block.box(0, 0, 4, 7, 0.01, 12),
            Block.box(7, 0, 6, 11, 1, 10),
            Block.box(9, 1, 6, 11, 2, 10),
            Block.box(11, 0, 5, 16, 3, 11)
    );
    public static final VoxelShape EAST_SHAPE = Shapes.or(
            Block.box(9, 0, 4, 16, 0.01, 12),
            Block.box(5, 0, 6, 9, 1, 10),
            Block.box(5, 1, 6, 7, 2, 10),
            Block.box(0, 0, 5, 5, 3, 11)
    );

    public StrippedCableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StrippedCableBlockEntity(pos, state);
    }
}
