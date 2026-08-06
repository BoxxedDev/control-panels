package moth.boxxed.panels.content.cable;

import moth.boxxed.panels.api.network.ModulesNetworkMemberBlock;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CableBlock extends ModulesNetworkMemberBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;

    private static final VoxelShape NORTH_SHAPE = Block.box(5, 0, 0, 11, 3, 5);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5, 0, 11, 11, 3, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(11, 0, 5, 16, 3, 11);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 5, 5, 3, 11);
    private static final VoxelShape CORE_SHAPE = Block.box(5, 0, 5, 11, 3, 11);
    private static final VoxelShape UP_SHAPE = Block.box(5, 3, 5, 11, 16, 11);

    public static final Map<Direction, BooleanProperty> directionPropertyMap = new HashMap<>();
    private static final Map<Direction, VoxelShape> directionShapeMap = new HashMap<>();
    static {
        directionPropertyMap.put(Direction.NORTH, CableBlock.NORTH);
        directionPropertyMap.put(Direction.SOUTH, CableBlock.SOUTH);
        directionPropertyMap.put(Direction.EAST, CableBlock.EAST);
        directionPropertyMap.put(Direction.WEST, CableBlock.WEST);
        directionPropertyMap.put(Direction.UP, CableBlock.UP);

        directionShapeMap.put(Direction.NORTH, NORTH_SHAPE);
        directionShapeMap.put(Direction.SOUTH, SOUTH_SHAPE);
        directionShapeMap.put(Direction.EAST, EAST_SHAPE);
        directionShapeMap.put(Direction.WEST, WEST_SHAPE);
        directionShapeMap.put(Direction.UP, UP_SHAPE);
    }

    public CableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isConnecting(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH);
        builder.add(EAST);
        builder.add(SOUTH);
        builder.add(WEST);
        builder.add(UP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape output = CORE_SHAPE;
        for (Direction direction : Direction.values()) {
            if (direction==Direction.DOWN) continue;
            if (state.getValue(directionPropertyMap.get(direction))) {
                output = Shapes.or(output, directionShapeMap.get(direction));
            }
        }
        return output;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState ret = this.defaultBlockState();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        for (Direction direction : Direction.values()) {
            if (direction==Direction.DOWN) continue;

            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            ret = ret.setValue(
                    directionPropertyMap.get(direction),
                    neighborState.getBlock() instanceof ModulesNetworkMemberBlock otherMember && this.isConnecting(level, pos, ret, direction) && otherMember.isConnecting(level, neighborPos, neighborState, direction.getOpposite())
            );
        }

        return ret;
    }


    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction==Direction.DOWN) return state;

        return state.setValue(
                directionPropertyMap.get(direction),
                neighborState.getBlock() instanceof ModulesNetworkMemberBlock otherMember && this.isConnecting(level, pos, state, direction) && otherMember.isConnecting(level, neighborPos, neighborState, direction.getOpposite())
        );
    }

    @Override
    public @org.jetbrains.annotations.Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(PanelItems.CABLE_STRIPPER)) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1f, 1f);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }
}
