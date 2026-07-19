package moth.boxxed.panels.api.panel;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.content.panel.normal.PanelBlock;
import moth.boxxed.panels.index.PanelTags;
import moth.boxxed.panels.util.BaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPanelBlock extends BaseEntityBlock {
    public static final EnumProperty<PanelBlock.Shape> SHAPE = EnumProperty.create("shape", PanelBlock.Shape.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AbstractPanelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (this.getBlockEntity(level, pos) != null) {
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
            ItemStack itemInHand = player.getMainHandItem();
//            if (itemInHand.is(PanelTags.Items.WRENCH)) {
//                if (!removeSelectedModule(level, pos, player)) {
//                    return ItemInteractionResult.SUCCESS;
//                }
//            }

            ItemInteractionResult result = this.getBlockEntity(level, pos).onItemUse(stack, state, level, pos, player, hitResult);
            if (result != null) {
                return result;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public AbstractPanelBlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity blockEntity)
            return blockEntity;
        return null;
    }

    private void openMenu(Player player, AbstractPanelBlockEntity be) {
        ((ServerPlayer)player).openMenu(be, be::sendToMenu);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe) {
                if (level instanceof ServerLevel) {
                    Containers.dropContents(level, pos, pbe.container);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE);
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        ItemStack inHandStack = player.getMainHandItem();
        if (inHandStack.is(PanelTags.Items.WRENCH) || inHandStack.is(PanelTags.Items.MODULE)) {
            return removeSelectedModule(level, pos, player);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    public boolean removeSelectedModule(Level level, BlockPos pos, Player player) {
        Dashpanels.LOGGER.debug("Client : {}", level.isClientSide);
        AbstractPanelBlockEntity pbe = this.getBlockEntity(level, pos);
        String module = pbe.getSelectedModule(player);
        if (module != null) {
            pbe.setSelectedModule(player, null);
            Module removedModule = pbe.removeModule(module);
            if (!player.isCreative() && removedModule != null) {
                ItemStack stack = new ItemStack(ModuleType.getItemFromType(removedModule.type));
                Inventory inventory = player.getInventory();
                int slot = inventory.getSlotWithRemainingSpace(stack);
                inventory.add(slot, stack);
            }
            pbe.setChanged();
            pbe.blockChanged();
        }
        return false;
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
