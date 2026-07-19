package moth.boxxed.panels.api.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.api.module.PlacementManager;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.screen.PanelMenu;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.util.Rect2d;
import moth.boxxed.panels.util.RectUtil;
import net.minecraft.client.renderer.Rect2i;
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
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class AbstractPanelBlockEntity extends ModulesNetworkMember implements MenuProvider, Clearable {
    public static final ModelProperty<ResourceLocation> SKIN_PROPERTY = new ModelProperty<>();

    public ModuleMap modules;
    public SimpleContainer container;
    public ResourceLocation skin;
    public PanelType panelType;

    private final Map<UUID, String> selectedModules = new HashMap<>();

    public AbstractPanelBlockEntity(PanelType panelType, BlockEntityType<? extends AbstractPanelBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.modules = new ModuleMap();
        //12*16 is 192 so like, I think that would be the max size.
        this.container = new SimpleContainer(192);
        this.panelType = panelType;
    }

    public boolean tryAddModule(String string, Module module) {
        Rect2d tempRect = new Rect2d(0,0,16,16);
        if (!tempRect.contains(module.rect))
            return false;

        for (Map.Entry<String, Module> existingModule : this.modules.entrySet()) {
            if (existingModule.getValue().inside(module.rect) && existingModule.getKey().equals(string))
                return false;
        }
        this.addModule(string, module);
        return true;
    }

    public void addModule(String string, Module module) {
        this.reconstructItems();
        module.name = string;
        module.parentBlockEntity = this;
        this.modules.put(string, module);
    }

    public Module removeModule(String module) {
        Module removedModule = this.modules.remove(module);
        this.reconstructItems();
        return removedModule;
    }

    public void reconstructItems() {
        this.container.clearContent();
        for (Map.Entry<String, Module> entry : this.modules) {
            Module module = entry.getValue();
            this.container.addItem(
                    new ItemStack(module.type.associatedItem)
            );
        }
    }

    public void clearModules() {
        this.modules.clear();
    }

    public Module getModule(String moduleName) {
        return this.modules.normalGet(moduleName);
    }

    @Override
    public ModuleMap getModules() {
        return this.modules;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
//        tag.putInt("modules_size", this.modules.size());
//        for (int i=0; i<this.modules.size(); i++) {
//            Map.Entry<String, Module> moduleEntry = this.modules.entrySet().stream().toList().get(i);
//            CompoundTag subTag = new CompoundTag();
//            if (moduleEntry.getValue().saveData(subTag, registries)) {
//                tag.put("module_%d".formatted(i), subTag);
//            }
//        }
        ListTag modulesTag = new ListTag(this.modules.size());
        for (Map.Entry<String, Module> entry : this.modules.entrySet()) {
            CompoundTag subTag = new CompoundTag();
            if (entry.getValue().saveData(subTag, registries)) {
                modulesTag.add(subTag);
            }
        }
        tag.put("modules", modulesTag);
        tag.putBoolean("new_loading", true);
        tag.put("container", this.container.createTag(registries));
        if (this.skin != null) {
            tag.putString("skin", this.skin.toString());
        } else {
            tag.putString("skin", this.panelType.defaultSkin.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int size = tag.getInt("modules_size");
        this.clearModules();
        if (tag.contains("new_loading")) {
            ListTag listTag = tag.getList("modules", 10);
            for (Tag moduleTag : listTag) {
                String typeString = ((CompoundTag) moduleTag).getString("type");
                ResourceLocation typeId = ResourceLocation.parse(typeString);
                Module module = Objects.requireNonNull(ModulesRegistry.MODULE_REGISTRY.get(typeId)).create(0, 0);
                module.loadData((CompoundTag) moduleTag, registries);
                this.addModule(module.getName(), module);
            }
        } else {
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
        this.requestModelDataUpdate();
        if (this.level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
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
        if (level.isClientSide)
            PanelModulesHitHandler.tick(this);
    }

    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        Module hitModule = this.getModule(this.selectedModules.computeIfAbsent(player.getUUID(), p -> ""));
        if (hitModule != null) {
            InteractionResult result = hitModule.onUse(level, player);
            if (!level.isClientSide)
                this.blockChanged();
            return result;
        }

        return InteractionResult.PASS;
    }

    public ItemInteractionResult onItemUse(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        Module hitModule = this.getModule(this.selectedModules.computeIfAbsent(player.getUUID(), p -> ""));
        if (hitModule != null) {
            ItemInteractionResult result = hitModule.onItemUse(stack, level, player);
            if (!level.isClientSide)
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

    public void setSelectedModule(Player player, String string) {
        this.selectedModules.put(player.getUUID(), string);
    }

    public String getSelectedModule(Player player) {
        return this.selectedModules.computeIfAbsent(player.getUUID(), player1 -> "");
    }

    @Override
    public void clearContent() {
        this.container.clearContent();
    }

    public void setSkin(ResourceLocation skin) {
        this.skin = skin;
        this.setChanged();
        this.blockChanged();
        Dashpanels.LOGGER.debug("Setting skin on the client : {}", this.getLevel().isClientSide);
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

    public boolean intersectsWithAnotherModule(Module module) {
        Rect2i moduleRect = module.getRect();
        for (Map.Entry<String, Module> entry : this.modules) {
            Rect2i otherModuleRect = entry.getValue().getRect();
            if (RectUtil.intersects(moduleRect, otherModuleRect)) {
                return true;
            }
        }
        return false;
    }

    public Map<UUID, String> getSelectedModules() {
        return new HashMap<>(this.selectedModules);
    }

    public abstract void transformPanelClipping(PoseStack stack);

    public abstract boolean canPlaceModuleOnSurface(Vec3 position, Direction face);

    public abstract Vector2i getPosForModule(Vec3 localSpace);

    public abstract BiConsumer<Module, PoseStack> getIndividualModuleTransform();

    public abstract void renderTransform(PoseStack poseStack);

    public abstract Vector2i getContentArea();
}
