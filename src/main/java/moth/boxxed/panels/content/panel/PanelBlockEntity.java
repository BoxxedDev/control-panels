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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PanelBlockEntity extends ModulesNetworkMember implements MenuProvider {
    public ModuleMap modules;
    public SimpleContainer container;

    public PanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.PANEL.get(), pos, blockState);
        this.modules = new ModuleMap();
        //12*16 is 192 so like, I think that would be the max size.
        this.container = new SimpleContainer(192);
    }

    public boolean tryAddModule(String string, Module module) {
        Rect2d tempRect = new Rect2d(0,0,16,16);
        if (!tempRect.contains(module.rect))
            return false;

        for (Map.Entry<String, Module> existingModule : this.modules.entrySet()) {
            if (existingModule.getValue().inside(module.rect) && existingModule.getKey().equals(string))
                return false;
        }
        module.name = string;
        module.parentBlockEntity = this;
        this.addModule(string, module);
        return true;
    }

    public void addModule(String string, Module module) {
        this.reconstructItems();
        this.modules.put(string, module);
    }

    public void reconstructItems() {
        this.container.clearContent();
        for (Map.Entry<String, Module> entry : this.modules) {
            Module module = entry.getValue();
            this.container.addItem(
                    new ItemStack(module.type.associatedItem.get())
            );
        }
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
            if (moduleEntry.getValue().saveData(subTag, registries)) {
                tag.put("module_%d".formatted(i), subTag);
            }
        }
        tag.put("container", this.container.createTag(registries));
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
            module.loadData(subTag, registries);
            this.tryAddModule(module.getName(), module);
        }
        this.container.clearContent();
        ListTag items = tag.getList("container", 10);
        for (Tag itemTag : items) {
            this.container.addItem(ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY));
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
        Vec3 localHitCoordinates = hitResult.getLocation().subtract(pos.getBottomCenter()).yRot((float) Math.toRadians(state.getValue(PanelBlock.FACING).toYRot())).add(0, 0, -0.25f);
        Rect2d rect = new Rect2d(-0.5, -0.5, 0.5, .25);
        boolean isInPanel = (localHitCoordinates.y>=0.75f && localHitCoordinates.y <= 1f) && rect.contains(localHitCoordinates.x, localHitCoordinates.z);
        if (!isInPanel) return InteractionResult.PASS;

        Module hitModule = getHitModule(player);
        if (hitModule != null) {
            if (!level.isClientSide)
                this.blockChanged();
            InteractionResult result = hitModule.onUse(level, player);
            if (!level.isClientSide)
                this.blockChanged();
            return result;
        }

        return InteractionResult.PASS;
    }

    public ItemInteractionResult onItemUse(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        Vec3 localHitCoordinates = hitResult.getLocation().subtract(pos.getBottomCenter()).yRot((float) Math.toRadians(state.getValue(PanelBlock.FACING).toYRot())).add(0, 0, -0.25f);
        Rect2d rect = new Rect2d(-0.5, -0.5, 0.5, .25);
        boolean isInPanel = (localHitCoordinates.y>=0.75f && localHitCoordinates.y <= 1f) && rect.contains(localHitCoordinates.x, localHitCoordinates.z);
        if (!isInPanel) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        Module hitModule = getHitModule(player);
        if (hitModule != null) {
            if (!level.isClientSide)
                this.blockChanged();
            ItemInteractionResult result = hitModule.onItemUse(stack, level, player);
            if (!level.isClientSide)
                this.blockChanged();
            return result;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private Module getHitModule(Player player) {
        Module hitModule = null;
        double hitDistance = Double.MAX_EXPONENT;
        for (Map.Entry<String, Module> entry : this.modules.entrySet()) {
            Module module = entry.getValue();
            Vec3 eyePos = player.getEyePosition();
            Double result = Module.clipModule(
                    this,
                    module,
                    new Vec3(module.getPos().x/16f, 0.75, module.getPos().y/16f),
                    eyePos,
                    player.getViewVector(1),
                    1
            );
            if (result != null && result < hitDistance) {
                hitDistance = result;
                hitModule = module;
            }
        }
        return hitModule;
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
        Set<String> takenNames = this.getOrCreate().compiledModules.keySet();
        buf.writeCollection(takenNames, ByteBufCodecs.STRING_UTF8);
    }
}
