package moth.boxxed.panels.compat.create.panel_link;

import com.simibubi.create.Create;
import moth.boxxed.panels.api.module.ModuleIOInfo;
import moth.boxxed.panels.api.network.ModulesNetwork;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.compat.create.panel_link.screen.PanelLinkMenu;
import moth.boxxed.panels.content.cable.CableBlock;
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

//Much of this was inspired by the linked typewriter from create: simulated, a lot of concepts I didn't think about until looking at their codebase
public class PanelLinkBlockEntity extends ModulesNetworkMember implements MenuProvider {
    private ModuleLinkEntries entries = new ModuleLinkEntries();

    public PanelLinkBlockEntity(BlockPos pos, BlockState state) {
        super(PanelCreateRegistries.PANEL_LINK_BE.get(), pos, state);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof PanelLinkBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());

        return to.getBlock() instanceof CableBlock && (!direction.getAxis().isVertical());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.entries = ModuleLinkEntries.fromTag(tag.getList("module_entries", 10), registries, this);
        for (ModuleLinkEntries.ModuleEntry entry : this.entries.getMap().values()) {
            entry.setPos(this.getBlockPos());
            entry.setBe(this);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("module_entries", this.entries.asTag(registries));
    }

    public void loadClient(CompoundTag tag, RegistryAccess registryAccess) {
        this.loadAdditional(tag, registryAccess);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(PanelCreateRegistries.PANEL_LINK.get().getDescriptionId());
    }

    @Override
    public void networkUpdate(ModulesNetwork modulesNetwork) {
        super.networkUpdate(modulesNetwork);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        this.entries.updateNetworks(level);
        super.tick(level, blockPos, blockState);
    }

    @Override
    public void remove() {
        super.remove();
        this.entries.clearFromNetworks(getLevel());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        this.networkUpdate(this.getOrCreate());
        return new PanelLinkMenu(containerId, playerInventory, this);
    }

    public void sendToMenu(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.getBlockPos());
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, buf.registryAccess());
        buf.writeNbt(tag);
        this.getOrCreate().compileModules();
        buf.writeCollection(this.getOrCreate().getCompiledModules().filterIOModules(), (buffer, val) -> ModuleIOInfo.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, val));
    }

    public ModuleLinkEntries getModuleEntries() {
        return this.entries;
    }
}
