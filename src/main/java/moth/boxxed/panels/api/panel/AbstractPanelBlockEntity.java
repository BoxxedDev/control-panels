package moth.boxxed.panels.api.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.*;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.config.gui.ModuleConfigScreenOpener;
import moth.boxxed.panels.api.module.placement.PlacementManager;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.index.PanelTags;
import moth.boxxed.panels.util.FlatAABB;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.joml.Vector2i;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public abstract class AbstractPanelBlockEntity extends ModulesNetworkMember implements Clearable {
    public static final ModelProperty<ResourceLocation> SKIN_PROPERTY = new ModelProperty<>();

    public ModuleMap modules;
    public SimpleContainer container;
    public NonNullList<Container> subContainers;

    public ResourceLocation skin;
    public int skinColor = 0xFFFFFF;

    private ResourceLocation cSkin;
    private Integer cSkinColor;

    public PanelType panelType;

    private final Map<UUID, String> selectedModules = new HashMap<>();
    private final Map<UUID, Vec3> hitPositions = new HashMap<>();

    public AbstractPanelBlockEntity(PanelType panelType, BlockEntityType<? extends AbstractPanelBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.modules = new ModuleMap();
        //12*16 is 192 so like, I think that would be the max size.
        this.container = new SimpleContainer(192);
        this.subContainers = NonNullList.create();
        this.panelType = panelType;
    }

    public boolean tryAddModule(String string, Module module) {
        PolyVoxel moduleShape = module.getShape().rotate(module.getRotation().getAngle()).move(module.getPos().x, module.getPos().y);
        if (!this.containsShape(moduleShape))
            return false;

        if (this.collidesWithOther(module)) {
            return false;
        }

        this.addModule(string, module);
        return true;
    }

    public void addModule(String string, Module module) {
        this.reconstructItems();
        module.setName(string);
        module.setParentBE(this);
        this.modules.put(string, module);
    }

    public Module removeModule(String module) {
        Module removedModule = this.modules.remove(module);
        this.reconstructItems();
        return removedModule;
    }

    public void reconstructItems() {
        this.container.clearContent();
        this.subContainers.clear();
        for (Map.Entry<String, Module> entry : this.modules) {
            Module module = entry.getValue();
            this.container.addItem(
                    new ItemStack(module.type.associatedItem)
            );

            if (module instanceof Container container) {
                this.subContainers.add(container);
            }
        }
    }

    public void clearModules() {
        this.modules.clear();
    }

    public Module getModule(String moduleName) {
        return this.modules.get(moduleName);
    }

    public Module getModuleAt(int x, int y) {
        for (Module module : this.modules.values()) {
            if (module.getPos().x == x && module.getPos().y == y) {
                return module;
            }
        }
        return null;
    }

    public void renameModule(String originalName, String newName) {
        Module module = this.modules.remove(originalName);
        if (module != null) {
            this.tryAddModule(newName, module);
        }
        setChanged();
        blockChanged();
    }

    @Override
    public ModuleMap getModules() {
        return this.modules;
    }

    public void saveExternal(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("modules", this.modules.asTag(registries));
        tag.putBoolean("new_loading", true);
        tag.put("container", this.container.createTag(registries));
        if (this.skin != null) {
            tag.putString("skin", this.skin.toString());
        } else {
            tag.putString("skin", this.panelType.defaultSkin.toString());
        }
        tag.putInt("skin_color", this.skinColor);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.saveExternal(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        //TODO: remove this in a future patch so it's always new loading without this extra boolean tag
        if (tag.contains("new_loading")) {
            Set<String> modulesLoaded = new HashSet<>();
            ListTag listTag = tag.getList("modules", 10);
            for (Tag moduleTag : listTag) {
                String typeString = ((CompoundTag) moduleTag).getString("type");
                String name = ((CompoundTag) moduleTag).getString("name");

                if (this.modules.containsKey(name)) {
                    Module module = this.getModule(name);
                    module.loadData((CompoundTag) moduleTag, registries);
                    module.setParentBE(this);
                } else {
                    ResourceLocation typeId = ResourceLocation.parse(typeString);
                    Module module = Objects.requireNonNull(ModulesRegistry.MODULE_REGISTRY.get(typeId)).create(0, 0);
                    module.setParentBE(this);
                    module.loadData((CompoundTag) moduleTag, registries);
                    this.addModule(module.getName(), module);
                }
                modulesLoaded.add(name);
            }

            for (String toRemove : this.modules.keySet().stream()
                    .filter(str -> !modulesLoaded.contains(str))
                    .collect(Collectors.toSet())) {
                this.removeModule(toRemove);
            }
        } else {
            this.clearModules();
            int size = tag.getInt("modules_size");
            for (int i=0; i<size; i++) {
                CompoundTag subTag = (CompoundTag) tag.get("module_%d".formatted(i));
                if (subTag == null) continue;
                ResourceLocation typeId = ResourceLocation.parse(subTag.getString("type"));
                Module module = Objects.requireNonNull(ModulesRegistry.MODULE_REGISTRY.get(typeId)).create(0, 0);
                module.loadData(subTag, registries);
                this.tryAddModule(module.getName(), module);
            }
        }
        this.container.clearContent();
        ListTag items = tag.getList("container", 10);
        for (Tag itemTag : items) {
            this.container.addItem(ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY));
        }
        this.skin = ResourceLocation.parse(tag.getString("skin"));
        this.skinColor = tag.getInt("skin_color");

        if (this.level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);

            if (this.level.isClientSide) {
                if (this.skin != this.cSkin || this.skinColor != this.cSkinColor) {
                    this.requestModelDataUpdate();
                    this.cSkin = this.skin;
                    this.cSkinColor = this.skinColor;
                }
            }
        }
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        super.tick(level, blockPos, blockState);
        for (Map.Entry<String, Module> module : this.modules.entrySet()) {
            module.getValue().tick(level, blockPos, blockState);
        }
        if (level.isClientSide)
            PanelModulesHitHandler.tick(this);
    }

    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        Module hitModule = this.getModule(this.selectedModules.computeIfAbsent(player.getUUID(), p -> ""));
        Vec3 hitPosition = this.hitPositions.get(player.getUUID());
        if (hitModule != null && hitPosition != null) {
            InteractionResult result = hitModule.onUse(new ModuleHitResult(hitPosition), level, player);
            this.setChanged();
            this.blockChanged();
            return result;
        }

        return InteractionResult.PASS;
    }

    public ItemInteractionResult onItemUse(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (stack.is(PanelTags.Items.WRENCH)) return ItemInteractionResult.SUCCESS;
        if (stack.is(PanelItems.PAINT_BRUSH.asItem())) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        Module hitModule = this.getModule(this.selectedModules.computeIfAbsent(player.getUUID(), p -> ""));
        Vec3 hitPosition = this.hitPositions.get(player.getUUID());
        if (hitModule != null && hitPosition != null) {
            ItemInteractionResult result = hitModule.onItemUse(new ModuleHitResult(hitPosition), stack, level, player);
            this.setChanged();
            this.blockChanged();
            return result;
        }

        if (level.isClientSide()) {
            if (PlacementManager.tryPlaceModule()) {
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public void setSelectedModule(Player player, Vec3 location, String string) {
        this.selectedModules.put(player.getUUID(), string);
        this.hitPositions.put(player.getUUID(), location);
    }

    public String getSelectedModule(Player player) {
        return this.selectedModules.computeIfAbsent(player.getUUID(), player1 -> "");
    }

    public Map<UUID, String> getSelectedModules() {
        return new HashMap<>(this.selectedModules);
    }

    public Vec3 getSelectedPosition(Player player) {
        return this.hitPositions.get(player.getUUID());
    }

    public Map<UUID, Vec3> getHitPositions() {
        return new HashMap<>(this.hitPositions);
    }

    @Override
    public void clearContent() {
        this.container.clearContent();
        for (Container subContainer : this.subContainers) {
            subContainer.clearContent();
        }
    }

    public void setSkin(ResourceLocation skin) {
        this.skin = skin;
        this.setChanged();
        this.blockChanged();
    }

    public void setSkinColor(int color) {
        this.skinColor = color;
        this.setChanged();
        this.blockChanged();
    }

    @Override
    public ModelData getModelData() {
        if (this.skin == null)
            return super.getModelData();
        if (this.skin.equals(this.panelType.defaultSkin))
            return super.getModelData();
        return ModelData.builder()
                .with(SKIN_PROPERTY, this.skin)
                .build();
    }

    public boolean collidesWithOther(Module module) {
        PolyVoxel moduleVoxel = module.getShape().rotate(module.getRotation().getAngle()).move(module.getPos().x, module.getPos().y);
        for (Map.Entry<String, Module> entry : this.modules) {
            Module other = entry.getValue();
            if (other == module)
                continue;
            PolyVoxel otherVoxel = entry.getValue().getShape().rotate(other.getRotation().getAngle()).move(other.getPos().x, other.getPos().y);
            if (otherVoxel.collides(moduleVoxel)) {
                return true;
            }
        }
        return false;
    }

    public boolean polyVoxelCollidesModules(PolyVoxel polyVoxel, String... exceptions) {
        Set<String> exceptionSet = Arrays.stream(exceptions).collect(Collectors.toSet());
        for (Map.Entry<String, Module> entry : this.modules) {
            if (exceptionSet.contains(entry.getKey()))
                continue;
            Module other = entry.getValue();
            PolyVoxel otherVoxel = entry.getValue().getShape().rotate(other.getRotation().getAngle()).move(other.getPos().x, other.getPos().y);
            if (otherVoxel.collides(polyVoxel)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsShape(PolyVoxel shape) {
        Vector2i contentArea = this.getContentArea();
        FlatAABB contentAABB = new FlatAABB(0, 0, contentArea.x, contentArea.y);
        return contentAABB.contains(shape.getBounds());
    }

    public boolean removeSelectedModule(Player player) {
        String module = this.getSelectedModule(player);
        if (module != null) {
            Module actualModule = this.getModule(module);
            if (actualModule == null)
                return true;
            if (!actualModule.canRemove(player)) {
                this.setChanged();
                this.blockChanged();
                return false;
            }

            this.setSelectedModule(player, null, null);
            Module removedModule = this.removeModule(module);
            removedModule.onRemove(player);

            if (removedModule instanceof Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (!stack.isEmpty()) {
                        Inventory inventory = player.getInventory();
                        int slot = inventory.getSlotWithRemainingSpace(stack);
                        inventory.add(slot, stack);
                    }
                }
            }

            if (!player.isCreative() && removedModule != null) {
                ItemStack stack = new ItemStack(ModuleType.getItemFromType(removedModule.type));
                Inventory inventory = player.getInventory();
                int slot = inventory.getSlotWithRemainingSpace(stack);
                inventory.add(slot, stack);
            }
            this.setChanged();
            this.blockChanged();
            return true;
        }
        return false;
    }

    public boolean openConfigureScreen(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            if (PlacementManager.isMovingModule()) {
                PlacementManager.stopMoving();
                return true;
            }
        }

        String moduleName = this.getSelectedModule(player);
        if (moduleName != null && level.isClientSide()) {
            Module module = this.getModule(moduleName);
            ModuleConfigScreenOpener.open(module, pos);
            return true;
        }
        return false;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        for (Module module : this.modules.values()) {
            module.onUnloaded();
        }
    }

    @Override
    public void remove() {
        for (Module module : this.modules.values()) {
            module.onRemove(null);
        }
        super.remove();
    }

    public abstract void transformPanelClipping(PoseStack stack);

    public abstract boolean canPlaceModuleOnSurface(Vec3 position, Direction face);

    public abstract Vector2i getPosForModule(Vec3 localSpace);

    public abstract BiConsumer<Module, PoseStack> getIndividualModuleTransform();

    public abstract void renderTransform(PoseStack poseStack);

    public abstract Vector2i getContentArea();
}
