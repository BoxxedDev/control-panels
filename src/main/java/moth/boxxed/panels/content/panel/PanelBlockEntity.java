package moth.boxxed.panels.content.panel;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.panel.screen.PanelMenu;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.util.Rect2d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class PanelBlockEntity extends ModulesNetworkMember implements MenuProvider {
    public ModuleMap modules;

    public PanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.PANEL.get(), pos, blockState);
        this.modules = new ModuleMap();
    }

    public void tryAddModule(String string, Module module) {
        Rect2d tempRect = new Rect2d(0,0,16,16);
        if (!tempRect.contains(module.rect))
            return;

        for (Map.Entry<String, Module> existingModule : this.modules.entrySet()) {
            if (existingModule.getValue().inside(module.rect) && existingModule.getKey().equals(string))
                return;
        }
        module.name = string;
        module.parentBlockEntity = this;
        this.addModule(string, module);
    }

    public void addModule(String string, Module module) {
        this.modules.put(string, module);
    }

    public void clearModules() {
        this.modules.clear();
    }

    public Module getModule(String moduleName) {
        return this.modules.get(moduleName);
    }

    @Override
    public ModuleMap getModules() {
        return this.modules;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("modules_size", this.modules.size());
        for (int i=0; i<this.modules.size(); i++) {
            Map.Entry<String, Module> moduleEntry = this.modules.entrySet().stream().toList().get(i);
            CompoundTag subTag = new CompoundTag();
            if (moduleEntry.getValue().saveData(subTag)) {
                tag.put("module_%d".formatted(i), subTag);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int size = tag.getInt("modules_size");
        this.clearModules();
        for (int i=0; i<size; i++) {
            CompoundTag subTag = (CompoundTag) tag.get("module_%d".formatted(i));
            if (subTag == null) continue;
            ResourceLocation typeId = ResourceLocation.parse(subTag.getString("type"));
            Module module = Objects.requireNonNull(ModulesRegistry.MODULE_REGISTRY.get(typeId)).create(0, 0);
            module.loadData(subTag);
            this.tryAddModule(module.getName(), module);
        }
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof PanelBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());
        Direction fromDirection = from.getValue(PanelBlock.FACING);

        if (direction.getAxis().isVertical())
            return false;
        if (fromDirection.getOpposite()==direction && to.getBlock() instanceof CableBlock)
            return true;
        return (fromDirection.getClockWise()==direction || fromDirection.getCounterClockWise()==direction) &&
                to.getBlock() instanceof PanelBlock &&
                from.getValue(PanelBlock.FACING) == to.getValue(PanelBlock.FACING);
    }

    public void loadClient(CompoundTag tag, HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        super.tick(level, blockPos, blockState);
        for (Map.Entry<String, Module> module : this.modules.entrySet()) {
            module.getValue().tick(level, blockPos, blockState);
        }
    }

    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.PASS;

        Vec3 localHitCoordinates = hitResult.getLocation().subtract(pos.getBottomCenter()).yRot((float) Math.toRadians(state.getValue(PanelBlock.FACING).toYRot())).add(0, 0, -0.25f);
        Rect2d rect = new Rect2d(-0.5, -0.5, 0.5, .25);
        boolean isInPanel = (localHitCoordinates.y==0.75f) && rect.contains(localHitCoordinates.x, localHitCoordinates.z);
        if (!isInPanel) return InteractionResult.PASS;

        for (Map.Entry<String, Module> entry : this.modules.entrySet()) {
            if (entry.getValue().inside((int) ((localHitCoordinates.x+0.5)*16), (int) ((localHitCoordinates.z+0.5)*16d))) {
                this.setChanged();
                if (level instanceof ServerLevel serverLevel)
                    serverLevel.getChunkSource().blockChanged(this.getBlockPos());
                return entry.getValue().onUse(level, player);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(PanelBlocks.CONTROL_PANEL.get().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PanelMenu(containerId, playerInventory, this);
    }

    public void sendToMenu(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.getBlockPos());
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, buf.registryAccess());
        buf.writeNbt(tag);
    }
}
