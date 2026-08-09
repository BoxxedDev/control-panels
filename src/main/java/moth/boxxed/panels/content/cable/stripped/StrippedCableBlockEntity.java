package moth.boxxed.panels.content.cable.stripped;

import moth.boxxed.panels.api.module.io.IOEntry;
import moth.boxxed.panels.api.module.io.ModuleIOInfo;
import moth.boxxed.panels.api.module.io.ModuleIOType;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.content.cable.stripped.screen.StrippedConfigMenu;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.index.PanelBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class StrippedCableBlockEntity extends ModulesNetworkMember implements MenuProvider {
    public IOEntry boundEntry = null;
    private int lastSignal = -1;

    public StrippedCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.STRIPPED_CABLE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.boundEntry != null) {
            Tag entryTag = this.boundEntry.asTag(registries);
            if (entryTag != null)
                tag.put("configured_entry", entryTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("configured_entry")) {
            this.boundEntry = IOEntry.fromTag(tag.get("configured_entry"), registries);
        } else if (tag.contains("configured_module")) {
            //Check to convert from old system to new (I hope)
            String name = tag.getString("configured_module");
            String assumedModuleName = name.lastIndexOf('-') < 0 ? name : name.substring(0, name.lastIndexOf('-') - 1);
            Optional<String> extension = name.lastIndexOf('-') >= 0 ? Optional.of(name.substring(name.lastIndexOf('-') + 1)) : Optional.empty();
            this.boundEntry = new IOEntry(assumedModuleName, null, extension);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(PanelBlocks.CONTROL_PANEL.get().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        this.getOrCreate().compileModules();
        return new StrippedConfigMenu(containerId, getOrCreate().getCompiledModules().filterIOModules(), this.getBlockPos(), this.boundEntry);
    }

    public void sendToMenu(RegistryFriendlyByteBuf buf) {
        this.getOrCreate().compileModules();
        buf.writeCollection(this.getOrCreate().getCompiledModules().filterIOModules(), (buffer, val) -> ModuleIOInfo.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, val));
        buf.writeBlockPos(this.getBlockPos());
        IOEntry.STREAM_CODEC.encode(buf, this.boundEntry != null ? this.boundEntry : new IOEntry("", ModuleIOType.INPUT, Optional.empty()));
    }

    public void setConfig(IOEntry module) {
        this.boundEntry = module;
        setChanged();
        blockChanged();
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        int signal = blockState.getSignal(level, blockPos, Direction.UP);
        if (signal != lastSignal) {
            lastSignal = signal;
            level.updateNeighborsAt(blockPos, blockState.getBlock());
        }
        super.tick(level, blockPos, blockState);
    }
}
