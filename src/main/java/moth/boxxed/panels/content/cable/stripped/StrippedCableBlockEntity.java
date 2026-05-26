package moth.boxxed.panels.content.cable.stripped;

import moth.boxxed.panels.api.network.connecting_panels.ConnectingModulesNetworkManager;
import moth.boxxed.panels.api.network.connecting_panels.INetworkMember;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class StrippedCableBlockEntity extends BlockEntity implements INetworkMember {
    public UUID network;
    private boolean chunkUnloaded;

    public StrippedCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.STRIPPED_CABLE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.network != null)
            tag.putUUID("network", this.network);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("network"))
            this.network = tag.getUUID("network");
        ConnectingModulesNetworkManager.getOrCreate(this);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.chunkUnloaded = true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!this.chunkUnloaded)
            ConnectingModulesNetworkManager.getOrCreate(this).removeMember(this.getBlockPos());
    }

    @Override
    public UUID getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(UUID network) {
        this.network = network;
    }
}
