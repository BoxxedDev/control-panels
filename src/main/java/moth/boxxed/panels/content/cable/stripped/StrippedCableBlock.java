package moth.boxxed.panels.content.cable.stripped;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.api.module.io.*;
import moth.boxxed.panels.api.network.ModulesNetwork;
import moth.boxxed.panels.api.network.ModulesNetworkMemberBlock;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StrippedCableBlock extends ModulesNetworkMemberBlock {
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
    public boolean isConnecting(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        Direction fromDirection = state.getValue(StrippedCableBlock.FACING);

        if (face.getAxis().isVertical())
            return false;
        return fromDirection.getOpposite() == face;
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

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        StrippedCableBlockEntity be = getBlockEntity(level, pos);
        if (be == null) return 0;
        if (be.boundEntry == null) return 0;
        ModulesNetwork network = be.getOrCreate();
        if (network == null) return 0;
        network.compileModules();
        ModuleMap map = network.getCompiledModules();
        Module module = map.get(be.boundEntry.name());
        if (module == null) return 0;
        be.setConfig(IOEntry.newEntryIfTypeNull(be.boundEntry, module));
        if (be.boundEntry == null) return 0;
        switch (module) {
            case IInput input when (!(module instanceof IMultiInput)) -> {
                return Math.clamp(input.getAnalog(), 0, 15);
            }
            case IMultiInput input when (!(module instanceof IInput)) && be.boundEntry.extension().isPresent() -> {
                Map<String, IMultiInput.AnalogResult> resultMap = new HashMap<>();
                input.getValues(resultMap::put);
                String extension = be.boundEntry.extension().get();
                IMultiInput.AnalogResult result = resultMap.get(extension);
                if (result != null) {
                    return Math.clamp(resultMap.get(extension).getAnalog(), 0, 15);
                }
            }
            default -> {
                return 0;
            }
        }
        return 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            StrippedCableBlockEntity be = getBlockEntity(level, pos);
            if (be == null) return;
            if (be.boundEntry == null) return;
            ModulesNetwork network = be.getOrCreate();
            if (network == null) return;
            ModuleMap map = network.getCompiledModules();
            Module module = map.get(be.boundEntry.name());
            if (module == null) return;
            be.setConfig(IOEntry.newEntryIfTypeNull(be.boundEntry, module));
            if (be.boundEntry == null) return;
            switch (module) {
                case IOutput output when (!(module instanceof IMultiOutput)) -> {
                    if (level.hasNeighborSignal(pos)) {
                        output.setAnalog(level.getBestNeighborSignal(pos));
                    } else {
                        output.setAnalog(0);
                    }
                    module.parentBlockEntity.networkUpdate(module.parentBlockEntity.getOrCreate());
                }
                case IMultiOutput output when (!(module instanceof IOutput)) && be.boundEntry.extension().isPresent() -> {
                    Map<String, IMultiOutput.AnalogRunnable> runnableMap = new HashMap<>();
                    output.setValues(runnableMap::put);
                    String extension = be.boundEntry.extension().get();
                    if (level.hasNeighborSignal(pos)) {
                        runnableMap.get(extension).setAnalog(level.getBestNeighborSignal(pos));
                    } else {
                        runnableMap.get(extension).setAnalog(0);
                    }
                }
                default -> {}
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(PanelItems.CABLE_STRIPPER)) {
            StrippedCableBlockEntity be = getBlockEntity(level, pos);
            if (be == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (player instanceof ServerPlayer serverPlayer)
                this.openMenu(serverPlayer, be);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public StrippedCableBlockEntity getBlockEntity(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof StrippedCableBlockEntity blockEntity)
            return blockEntity;
        return null;
    }

    private void openMenu(ServerPlayer player, StrippedCableBlockEntity be) {
        player.openMenu(be, be::sendToMenu);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return PanelBlocks.CABLE.toStack();
    }

    @Override
    public @org.jetbrains.annotations.Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
