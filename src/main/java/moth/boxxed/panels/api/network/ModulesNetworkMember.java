package moth.boxxed.panels.api.network;

import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.util.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public abstract class ModulesNetworkMember extends BaseBlockEntity {
    public UUID network = null;

    public ModulesNetworkMember(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (hasNetwork())
            tag.putString("network", this.network.toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("network"))
            this.network = UUID.fromString(tag.getString("network"));
    }

    public ModulesNetwork getOrCreate() {
        return ModulesNetworkManager.getNetwork(this);
    }

    public boolean hasNetwork() {
        return this.network != null;
    }

    public ModuleMap getModules() {
        return ModuleMap.empty();
    }

    @Override
    public void init() {
        if(!this.getLevel().isClientSide)
            ModulesNetworkManager.handleAddingMember(this);
    }

    @Override
    public void remove() {
        if (!this.getLevel().isClientSide && hasNetwork())
            this.getOrCreate().removeMember(this, true);
    }

    public void setNetwork(UUID uuid) {
        if (uuid.equals(this.network)) return;
        if (this.network != null) getOrCreate().removeMember(this, false);

        this.network = uuid;
        getOrCreate().addMember(this);
        setChanged();
    }
}
