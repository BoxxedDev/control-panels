package moth.boxxed.panels.api.network.connecting_panels;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.util.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public abstract class ModulesNetworkMember extends BaseBlockEntity {
    public UUID network;

    public ModulesNetworkMember(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (hasNetwork())
            tag.putUUID("network", this.network);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("network"))
            this.network = tag.getUUID("network");
    }

    public ModulesNetwork getOrCreate() {
        return ModulesNetworkManager.getNetwork(this);
    }

    public boolean hasNetwork() {
        return this.network != null;
    }

    public abstract boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to);

    public void networkUpdate(ModulesNetwork modulesNetwork) {
    }

    public ModuleMap getModules() {
        return ModuleMap.empty();
    }

    @Override
    public void init() {
        if (!getLevel().isClientSide)
            ModulesNetworkManager.handleAddingMember(this);
    }

    @Override
    public void remove() {
        if ((!getLevel().isClientSide) && hasNetwork()) {
            this.getOrCreate().removeMember(this);
        }
    }
}
