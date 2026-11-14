package moth.boxxed.panels.content.cable.stripped;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.io.IInput;
import moth.boxxed.panels.api.module.io.IMultiInput;
import moth.boxxed.panels.api.module.io.IMultiOutput;
import moth.boxxed.panels.api.module.io.IOEntry;
import moth.boxxed.panels.api.module.io.IOutput;
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

import java.util.Objects;
import java.util.Optional;

public class StrippedCableBlockEntity extends ModulesNetworkMember implements MenuProvider {
    public IOEntry boundEntry = null;
    public int lastSignal = 0;

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

    public void saveExternal(CompoundTag tag, HolderLookup.Provider registries) {
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
        return new StrippedConfigMenu(containerId, getOrCreate().getCompiledModules().filterIOModules(), this.getBlockPos(), this.boundEntry);
    }

    public void sendToMenu(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(this.getOrCreate().getCompiledModules().filterIOModules(), (buffer, val) -> ModuleIOInfo.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, val));
        buf.writeBlockPos(this.getBlockPos());
        IOEntry.STREAM_CODEC.encode(buf, this.boundEntry != null ? this.boundEntry : new IOEntry("", ModuleIOType.INPUT, Optional.empty()));
    }

    public void setConfig(IOEntry entry) {
        if (!Objects.equals(entry, this.boundEntry)) {
            this.boundEntry = entry;
            this.setChanged();
            this.sendSignal(this.getBlockState(), this.getLevel(), this.getBlockPos());
        }
    }

    public void setEntryIfNull(Module module) {
        IOEntry newEntry = IOEntry.newEntryIfTypeNull(this.boundEntry, module);
        if(newEntry == null) {
            this.setConfig(newEntry);
        }
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        super.tick(level, blockPos, blockState);
        int signal = this.getSignal();
        if (signal != lastSignal) {
            lastSignal = signal;
            level.updateNeighborsAt(blockPos, blockState.getBlock());
        }
    }

    public int getSignal(){
        if(this.boundEntry == null || !this.hasNetwork()) return 0;
        if(this.boundEntry.type() == ModuleIOType.OUTPUT || this.boundEntry.type() == ModuleIOType.MULTI_OUTPUT) return 0;

        Module mod = this.getOrCreate().getCompiledModules().get(this.boundEntry.name());
        if(mod == null) return 0;
        this.setEntryIfNull(mod);

        if (this.boundEntry.type() == ModuleIOType.INPUT && mod instanceof IInput input) {
            return Math.clamp(input.getAnalog(), 0, 15);
        } else if(this.boundEntry.type() == ModuleIOType.MULTI_INPUT && mod instanceof IMultiInput input) {
            IMultiInput.AnalogResult[] result = new IMultiInput.AnalogResult[1];
            String ext = this.boundEntry.extension().get();
            input.getValues((k, v) -> {
                if (Objects.equals(k, ext)) result[0] = v;
            });
            if(result[0] != null) return Math.clamp(result[0].getAnalog(), 0, 15);
        }
        return 0;
    }

    public void sendSignal(BlockState state, Level level, BlockPos pos){
        if (this.boundEntry == null || !this.hasNetwork()) return;
        if (this.boundEntry.type() == ModuleIOType.INPUT || this.boundEntry.type() == ModuleIOType.MULTI_INPUT) return;
        Module mod = this.getOrCreate().getCompiledModules().get(this.boundEntry.name());
        if (mod == null) return;
        this.setEntryIfNull(mod);

        Direction direction = state.getValue(StrippedCableBlock.FACING);
        int signal = level.getSignal(pos.relative(direction), direction);
        if (this.boundEntry.type() == ModuleIOType.OUTPUT && mod instanceof IOutput output) {
            output.setAnalog(signal);
        } else if (this.boundEntry.type() == ModuleIOType.MULTI_OUTPUT && mod instanceof IMultiOutput output) {
            IMultiOutput.AnalogRunnable[] runnable = new IMultiOutput.AnalogRunnable[1];
            String ext = this.boundEntry.extension().get();
            output.setValues((k, v) -> {
                if (Objects.equals(k, ext)) v.setAnalog(signal);
            });
        }
        mod.parentBlockEntity.blockChanged();
    }
}
