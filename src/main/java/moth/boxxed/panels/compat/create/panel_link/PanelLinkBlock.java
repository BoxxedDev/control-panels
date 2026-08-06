package moth.boxxed.panels.compat.create.panel_link;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.network.ModulesNetworkMemberBlock;
import moth.boxxed.panels.content.cable.CableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PanelLinkBlock extends ModulesNetworkMemberBlock implements IWrenchable {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public static final VoxelShape RENDER_SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 7, 15),
            Block.box(2, 7, 2, 14, 12, 14)
    );
    public static final VoxelShape COLLISION_SHAPE = Block.box(1,0,1,15,12,15);

    public static final Map<Direction, BooleanProperty> directionPropertyMap = new HashMap<>();
    static {
        directionPropertyMap.put(Direction.NORTH, CableBlock.NORTH);
        directionPropertyMap.put(Direction.SOUTH, CableBlock.SOUTH);
        directionPropertyMap.put(Direction.EAST, CableBlock.EAST);
        directionPropertyMap.put(Direction.WEST, CableBlock.WEST);
    }

    public PanelLinkBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isConnecting(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return !face.getAxis().isVertical();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH);
        builder.add(EAST);
        builder.add(SOUTH);
        builder.add(WEST);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState ret = defaultBlockState();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            boolean check1 = neighborState.getBlock() instanceof ModulesNetworkMemberBlock otherMember;
            boolean check2 = this.isConnecting(level, pos, ret, direction);
            boolean check3 = check1 && ((ModulesNetworkMemberBlock) neighborState.getBlock()).isConnecting(level, neighborPos, neighborState, direction.getOpposite());
            Dashpanels.LOGGER.debug("{} | {} | {} | {}", direction, check1, check2, check3);
            ret = ret.setValue(
                    directionPropertyMap.get(direction),
                    check1 && check2 && check3
            );
        }

        return ret;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis().isVertical()) return state;

        return state.setValue(
                directionPropertyMap.get(direction),
                neighborState.getBlock() instanceof ModulesNetworkMemberBlock otherMember && this.isConnecting(level, pos, state, direction) && otherMember.isConnecting(level, neighborPos, neighborState, direction.getOpposite())
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return RENDER_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        openMenu(level, pos, player);

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    private void openMenu(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof PanelLinkBlockEntity be && player instanceof ServerPlayer serverPlayer) {
            be.getModuleEntries().validate(be.getOrCreate());
            serverPlayer.openMenu(be, be::sendToMenu);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PanelLinkBlockEntity(pos, state);
    }
}
