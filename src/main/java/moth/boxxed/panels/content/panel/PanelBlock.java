package moth.boxxed.panels.content.panel;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.util.BaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PanelBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

    private static final VoxelShape MAIN_SHAPE = Block.box(0,0,0,16,12,16);
    private static final VoxelShape TOP_N_SHAPE = Block.box(0, 12, 12, 16, 16, 16);
    private static final VoxelShape TOP_S_SHAPE = Block.box(0, 12, 0, 16, 16, 4);
    private static final VoxelShape TOP_E_SHAPE = Block.box(0, 12, 0, 4, 16, 16);
    private static final VoxelShape TOP_W_SHAPE = Block.box(12, 12, 0, 16, 16, 16);

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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (this.getBlockEntity(level, pos) != null) {
            if (player.isShiftKeyDown() && !level.isClientSide()) {
                this.openMenu(player, this.getBlockEntity(level, pos));
                return ItemInteractionResult.SUCCESS;
            }
            ItemInteractionResult result = this.getBlockEntity(level, pos).onItemUse(stack, state, level, pos, player, hitResult);
            if (result != null)
                return result;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(SHAPE);
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

    public PanelBlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PanelBlockEntity blockEntity)
            return blockEntity;
        return null;
    }

    private void openMenu(Player player, PanelBlockEntity be) {
        ((ServerPlayer)player).openMenu(be, be::sendToMenu);
    }

    @Override
    public @org.jetbrains.annotations.Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
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
