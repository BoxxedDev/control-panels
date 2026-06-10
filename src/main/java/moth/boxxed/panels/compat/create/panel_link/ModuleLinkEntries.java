package moth.boxxed.panels.compat.create.panel_link;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.*;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.network.ModulesNetwork;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ModuleLinkEntries {
    private final Map<String, ModuleEntry> entryMap = new HashMap<>();

    private final Map<String, ModuleEntry> modulesToAdd = new HashMap<>();
    private final Map<String, ModuleEntry> modulesToRemove = new HashMap<>();

    public void updateNetworks(Level level) {
        if (!level.isClientSide) {
            for (ModuleEntry entry : this.modulesToRemove.values()) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(level, entry);
            }
            this.modulesToRemove.clear();

            for (ModuleEntry entry : this.modulesToAdd.values()) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, entry);
            }
            this.modulesToAdd.clear();
        }
    }

    public void clearFromNetworks(Level level) {
        if (!level.isClientSide) {
            for (ModuleEntry entry : this.entryMap.values()) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(level, entry);
            }
        }
    }

    public void add(ModuleEntry entry) {
        this.entryMap.put(entry.getModule(), entry);
        this.modulesToAdd.put(entry.getModule(), entry);
    }

    public void addAll(Map<String, ModuleEntry> entries) {
        this.entryMap.putAll(entries);
        this.modulesToAdd.putAll(entries);
    }

    public Map<String, ModuleEntry> getMap() {
        return this.entryMap;
    }

    public static ModuleLinkEntries fromTag(ListTag listTag, HolderLookup.Provider registryAccess, PanelLinkBlockEntity be) {
        ModuleLinkEntries entries = new ModuleLinkEntries();
        for (Tag tag : listTag) {
            RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
            DataResult<Pair<ModuleEntry, Tag>> result = ModuleEntry.CODEC.decode(ops, tag);
            if (result.isSuccess()) {
                ModuleEntry entry = result.getOrThrow().getFirst();
                entry.setPos(be.getBlockPos());
                entry.setBe(be);
                entries.set(entry.getModule(), entry);
            }
        }
        return entries;
    }

    public ListTag asTag(HolderLookup.Provider registryAccess) {
        ListTag tag = new ListTag();
        for (Map.Entry<String, ModuleEntry> entry : this.entryMap.entrySet()) {
            RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> result = ModuleEntry.CODEC.encodeStart(ops, entry.getValue());
            if (result.isSuccess()) {
                tag.add(result.getOrThrow());
            }
        }
        return tag;
    }

    public void set(String module, ModuleEntry entry) {
        if (entry == null) {
            ModuleEntry nonNullEntry = this.entryMap.remove(module);
            this.modulesToRemove.put(module, nonNullEntry);
            return;
        }

        this.entryMap.put(module, entry);
        this.modulesToAdd.put(module, entry);
    }

    public void validate(ModulesNetwork modulesNetwork) {
        this.entryMap.entrySet().removeIf(entry -> !modulesNetwork.hasModule(entry.getKey()));
    }

    public void clearAll() {
        this.entryMap.clear();
    }

    public static class ModuleEntry implements IRedstoneLinkable {
        public static final Codec<ModuleEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemStack.OPTIONAL_CODEC.fieldOf("first").forGetter(ModuleEntry::firstItemStack),
                        ItemStack.OPTIONAL_CODEC.fieldOf("second").forGetter(ModuleEntry::secondItemStack),
                        Codec.STRING.fieldOf("name").forGetter(ModuleEntry::getModule)
                ).apply(instance, ModuleEntry::fromCodec));

        public static final StreamCodec<RegistryFriendlyByteBuf, ModuleEntry> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, ModuleEntry::firstItemStack,
                ItemStack.OPTIONAL_STREAM_CODEC, ModuleEntry::secondItemStack,
                ByteBufCodecs.STRING_UTF8, ModuleEntry::getModule,
                ModuleEntry::fromCodec
        );

        private final RedstoneLinkNetworkHandler.Frequency first;
        private final RedstoneLinkNetworkHandler.Frequency second;

        private final String module;
        private BlockPos pos;

        public PanelLinkBlockEntity parentalBE;

        public ModuleEntry(RedstoneLinkNetworkHandler.Frequency first, RedstoneLinkNetworkHandler.Frequency second, String module, BlockPos pos) {
            if (first == null)
                first = RedstoneLinkNetworkHandler.Frequency.EMPTY;
            if (second == null)
                second = RedstoneLinkNetworkHandler.Frequency.EMPTY;

            this.first = first;
            this.second= second;

            this.pos = pos;
            this.module = module;
        }

        public static ModuleEntry fromCodec(ItemStack first, ItemStack second, String name) {
            RedstoneLinkNetworkHandler.Frequency firstFreq = RedstoneLinkNetworkHandler.Frequency.of(first);
            RedstoneLinkNetworkHandler.Frequency secondFreq = RedstoneLinkNetworkHandler.Frequency.of(second);
            return new ModuleEntry(firstFreq, secondFreq, name, null);
        }

        @Override
        public int getTransmittedStrength() {
            if (this.parentalBE == null)
                return 0;
            Module actualModule = this.parentalBE.getOrCreate().getCompiledModules().get(this.module);
            if (actualModule instanceof IInput input)
                return input.getAnalog();
            if (actualModule instanceof IMultiInput multiInput) {
                Map<String, IMultiInput.AnalogResult> resultMap = new HashMap<>();
                multiInput.getValues(resultMap::put);
                String extension = this.module.substring(actualModule.getName().length()+3);
                if (resultMap.get(extension) != null)
                    return resultMap.get(extension).getAnalog();
            }
            return 0;
        }

        @Override
        public void setReceivedStrength(int power) {
            if (this.parentalBE == null)
                return;
            Module actualModule = this.parentalBE.getOrCreate().getCompiledModules().get(this.module);
            if (actualModule instanceof IOutput output) {
                output.setAnalog(power);
                actualModule.parentBlockEntity.networkUpdate(actualModule.parentBlockEntity.getOrCreate());
            }
            if (actualModule instanceof IMultiOutput multiOutput) {
                Map<String, IMultiOutput.AnalogRunnable> runnableMap = new HashMap<>();
                multiOutput.setValues(runnableMap::put);
                String extension = this.module.substring(actualModule.getName().length()+3);
                if (runnableMap.get(extension) != null)
                    runnableMap.get(extension).setAnalog(power);
                actualModule.parentBlockEntity.networkUpdate(actualModule.parentBlockEntity.getOrCreate());
            }
        }

        @Override
        public boolean isListening() {
            if (this.parentalBE == null)
                return false;
            return this.parentalBE.getOrCreate().getCompiledModules().get(this.module) instanceof IOutput ||
                    this.parentalBE.getOrCreate().getCompiledModules().get(this.module) instanceof IMultiOutput;
        }

        @Override
        public boolean isAlive() {
            if (this.parentalBE == null)
                return false;
            Module gottenModule = this.parentalBE.getOrCreate().getCompiledModules().get(this.module);
            return gottenModule instanceof IInput ||
                    gottenModule instanceof IOutput ||
                    gottenModule instanceof IMultiInput ||
                    gottenModule instanceof IMultiOutput;
        }

        @Override
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return Couple.create(this.first, this.second);
        }

        @Override
        public BlockPos getLocation() {
            return this.pos;
        }

        public ItemStack firstItemStack() {
            return this.first.getStack();
        }

        public ItemStack secondItemStack() {
            return this.second.getStack();
        }

        public RedstoneLinkNetworkHandler.Frequency first() {
            return this.first;
        }

        public RedstoneLinkNetworkHandler.Frequency second() {
            return this.second;
        }

        public String getModule() {
            return this.module;
        }

        public void setPos(BlockPos pos) {
            this.pos = pos;
        }

        public void setBe(PanelLinkBlockEntity be) {
            this.parentalBE = be;
        }
    }
}
