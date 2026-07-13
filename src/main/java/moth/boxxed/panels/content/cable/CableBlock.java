package moth.boxxed.panels.content.cable;

import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlock;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlock;
import moth.boxxed.panels.content.panel.normal.PanelBlock;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.util.BaseEntityBlock;
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
import net.neoforged.fml.ModList;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CableBlock extends BaseEntityBlock {
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

        for (Direction direction : Direction.values()) {
            if (direction==Direction.DOWN) continue;

            BlockPos neighborPos = context.getClickedPos().relative(direction);
            BlockState neighborState = context.getLevel().getBlockState(neighborPos);

            boolean check1 = neighborState.getBlock() instanceof CableBlock;
            boolean check2 = neighborState.getBlock() instanceof PanelBlock && (neighborState.getValue(PanelBlock.FACING)==direction || direction==Direction.UP);
            boolean check3 = neighborState.getBlock() instanceof StrippedCableBlock && neighborState.getValue(StrippedCableBlock.FACING)==direction;
            boolean check4 = false;
            if (ModList.get().isLoaded("create"))
                check4 = neighborState.getBlock() instanceof PanelLinkBlock && !direction.getAxis().isVertical();

            ret = ret.setValue(
                    directionPropertyMap.get(direction),
                    check1 || check2 || check3 || check4
            );
        }

        return ret;
    }


    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos p3) {
        if (direction==Direction.DOWN) return state;

        boolean check1 = neighborState.hasBlockEntity() && state.hasBlockEntity();
        boolean check2 = neighborState.getBlock() instanceof CableBlock;
        boolean check3 = neighborState.getBlock() instanceof PanelBlock && (neighborState.getValue(PanelBlock.FACING)==direction || direction==Direction.UP);
        boolean check4 = neighborState.getBlock() instanceof StrippedCableBlock && neighborState.getValue(StrippedCableBlock.FACING)==direction;
        boolean check5 = false;
        if (ModList.get().isLoaded("create"))
            check5 = neighborState.getBlock() instanceof PanelLinkBlock && !direction.getAxis().isVertical();

        return state.setValue(
                directionPropertyMap.get(direction),
                check1 && (check2 || check3 || check4 || check5)
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
