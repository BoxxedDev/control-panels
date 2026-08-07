package moth.boxxed.panels.content.panel.normal;

import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.skin.ClientSkin;
import moth.boxxed.panels.api.panel.skin.PanelSkinsClientManager;
import moth.boxxed.panels.api.panel.skin.SkinShape;
import moth.boxxed.panels.index.PanelShapes;
import moth.boxxed.panels.util.HalfHalfVoxelShape;
import moth.boxxed.panels.util.OutlinedVoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

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
        if (FMLLoader.getDist().isClient() && level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe) {
            ClientSkin clientSkin = PanelSkinsClientManager.MAP.get(pbe.skin);
            if (clientSkin != null && clientSkin.shape().isPresent()) {
                if (clientSkin.shape().get().bounds().isPresent()) {
                    VoxelShape shape = Shapes.empty();
                    for (SkinShape.Bounds bounds : clientSkin.shape().get().bounds().get()) {
                        shape = Shapes.or(shape, bounds.toVoxelShape(clientSkin.shape().get().directional(), state.getValue(FACING)));
                    }
                    return new HalfHalfVoxelShape(
                            PanelShapes.PANEL_SHAPE.get(state.getValue(FACING)),
                            shape
                    );
                } else if (clientSkin.shape().get().lines().isPresent()) {
                    List<Pair<Vec3, Vec3>> list = new ArrayList<>();
                    for (SkinShape.Line line : clientSkin.shape().get().lines().get()) {
                        list.add(line.toPair());
                    }

                    return new OutlinedVoxelShape(
                            PanelShapes.PANEL_SHAPE.get(state.getValue(FACING)),
                            list
                            );
                }

            }
        }

        return PanelShapes.PANEL_SHAPE.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getVisualShape(state, level, pos, context);
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
