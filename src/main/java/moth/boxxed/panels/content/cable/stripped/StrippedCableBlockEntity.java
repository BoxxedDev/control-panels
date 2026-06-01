package moth.boxxed.panels.content.cable.stripped;

import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.cable.stripped.screen.StrippedConfigMenu;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.index.PanelBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class StrippedCableBlockEntity extends ModulesNetworkMember implements MenuProvider {
    public String boundModule = "";

    public StrippedCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.STRIPPED_CABLE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.boundModule != null)
            tag.putString("configured_module", this.boundModule);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("configured_module"))
            this.boundModule = tag.getString("configured_module");
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof StrippedCableBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());
        Direction fromDirection = from.getValue(PanelBlock.FACING);

        if (direction.getAxis().isVertical())
            return false;
        return to.getBlock() instanceof CableBlock && fromDirection.getOpposite().equals(direction);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(PanelBlocks.CONTROL_PANEL.get().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new StrippedConfigMenu(containerId, getOrCreate().getCompiledModules(), this.getBlockPos(), this.boundModule);
    }

    public void sendToMenu(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = getOrCreate().getCompiledModules().asTag();
        buf.writeNbt(tag);
        buf.writeBlockPos(this.getBlockPos());
        buf.writeUtf(this.boundModule);
    }

    public void setConfig(String module) {
        if (getOrCreate().hasModule(module))
            this.boundModule = module;
        setChanged();
        blockChanged();
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        level.updateNeighborsAt(blockPos, blockState.getBlock());
        super.tick(level, blockPos, blockState);
    }
}
