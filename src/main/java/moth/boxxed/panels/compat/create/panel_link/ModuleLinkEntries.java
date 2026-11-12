package moth.boxxed.panels.compat.create.panel_link;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.io.*;
import moth.boxxed.panels.api.network.ModulesNetwork;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ModuleLinkEntries {
    private final Map<IOEntry, ModuleEntry> entryMap = new HashMap<>();

    private final Map<IOEntry, ModuleEntry> modulesToAdd = new HashMap<>();
    private final Map<IOEntry, ModuleEntry> modulesToRemove = new HashMap<>();

    public void updateNetworks(Level level) {
        if (!level.isClientSide) {
            //Create new array so ConcurrentModificationException does not occur
            for (ModuleEntry entry : new ArrayList<>(this.modulesToRemove.values())) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(level, entry);
            }
            this.modulesToRemove.clear();

            for (ModuleEntry entry : new ArrayList<>(this.modulesToAdd.values())) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, entry);
            }
            this.modulesToAdd.clear();
        }
        for (ModuleEntry entry : new ArrayList<>(this.entryMap.values())) {
            if(entry.sending != entry.update())
                Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(level, entry);
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
        this.entryMap.put(entry.getEntry(), entry);
        this.modulesToAdd.put(entry.getEntry(), entry);
    }

    public void addAll(Map<IOEntry, ModuleEntry> entries) {
        this.entryMap.putAll(entries);
        this.modulesToAdd.putAll(entries);
    }

    public Map<IOEntry, ModuleEntry> getMap() {
        return this.entryMap;
    }

    public static ModuleLinkEntries fromOldTag(ListTag listTag, HolderLookup.Provider registryAccess, PanelLinkBlockEntity be) {
        ModuleLinkEntries entries = new ModuleLinkEntries();
        for (Tag tag : listTag) {
            RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
            DataResult<Pair<ModuleEntry, Tag>> result = ModuleEntry.OLD_DECODER.decode(ops, tag);
            if (result.isSuccess()) {
                ModuleEntry entry = result.getOrThrow().getFirst();
                entry.setPos(be.getBlockPos());
                entry.setBe(be);
                entries.set(entry.getEntry(), entry);
            }
        }
        return entries;
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
                entries.set(entry.getEntry(), entry);
            }
        }
        return entries;
    }

    public ListTag asTag(HolderLookup.Provider registryAccess) {
        ListTag tag = new ListTag();
        for (Map.Entry<IOEntry, ModuleEntry> entry : this.entryMap.entrySet()) {
            RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> result = ModuleEntry.CODEC.encodeStart(ops, entry.getValue());
            if (result.isSuccess()) {
                tag.add(result.getOrThrow());
            }
        }
        return tag;
    }

    public void set(IOEntry ioEntry, ModuleEntry entry) {
        if (entry == null) {
            ModuleEntry nonNullEntry = this.entryMap.remove(ioEntry);
            this.modulesToRemove.put(ioEntry, nonNullEntry);
            return;
        }

        this.entryMap.put(ioEntry, entry);
        this.modulesToAdd.put(ioEntry, entry);
    }

    public void validate(ModulesNetwork modulesNetwork) {
        for(IOEntry entry : new HashSet<>(this.entryMap.keySet())) {
            if (!modulesNetwork.hasModule(entry.name())) {
                this.set(entry, null);
            }
        }
    }

    public void clearAll() {
        this.modulesToRemove.putAll(this.entryMap);
        this.entryMap.clear();
    }

    public void addAllToNetworks(Level level) {
        if (!level.isClientSide) {
            for (ModuleEntry entry : new ArrayList<>(this.entryMap.values())) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, entry);
            }
        }
    }

    public static class ModuleEntry implements IRedstoneLinkable {
        public static final Decoder<ModuleEntry> OLD_DECODER = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemStack.OPTIONAL_CODEC.fieldOf("first").forGetter(ModuleEntry::firstItemStack),
                        ItemStack.OPTIONAL_CODEC.fieldOf("second").forGetter(ModuleEntry::secondItemStack),
                        Codec.STRING.fieldOf("name").forGetter(entry -> entry.getEntry().toString())
                ).apply(instance, ModuleEntry::fromOldCodec));

        public static final Codec<ModuleEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemStack.OPTIONAL_CODEC.fieldOf("first").forGetter(ModuleEntry::firstItemStack),
                        ItemStack.OPTIONAL_CODEC.fieldOf("second").forGetter(ModuleEntry::secondItemStack),
                        IOEntry.CODEC.fieldOf("entry").forGetter(ModuleEntry::getEntry)
                ).apply(instance, ModuleEntry::fromCodec));

        public static final StreamCodec<RegistryFriendlyByteBuf, ModuleEntry> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, ModuleEntry::firstItemStack,
                ItemStack.OPTIONAL_STREAM_CODEC, ModuleEntry::secondItemStack,
                IOEntry.STREAM_CODEC, ModuleEntry::getEntry,
                ModuleEntry::fromCodec
        );

        private final RedstoneLinkNetworkHandler.Frequency first;
        private final RedstoneLinkNetworkHandler.Frequency second;

        private IOEntry entry;
        private BlockPos pos;

        public PanelLinkBlockEntity parentalBE;

        public ModuleEntry(RedstoneLinkNetworkHandler.Frequency first, RedstoneLinkNetworkHandler.Frequency second, IOEntry entry, BlockPos pos) {
            if (first == null)
                first = RedstoneLinkNetworkHandler.Frequency.EMPTY;
            if (second == null)
                second = RedstoneLinkNetworkHandler.Frequency.EMPTY;

            this.first = first;
            this.second = second;

            this.pos = pos;
            this.entry = entry;
        }

        public static ModuleEntry fromCodec(ItemStack first, ItemStack second, IOEntry entry) {
            RedstoneLinkNetworkHandler.Frequency firstFreq = RedstoneLinkNetworkHandler.Frequency.of(first);
            RedstoneLinkNetworkHandler.Frequency secondFreq = RedstoneLinkNetworkHandler.Frequency.of(second);
            return new ModuleEntry(firstFreq, secondFreq, entry, null);
        }

        public static ModuleEntry fromOldCodec(ItemStack first, ItemStack second, String name) {
            RedstoneLinkNetworkHandler.Frequency firstFreq = RedstoneLinkNetworkHandler.Frequency.of(first);
            RedstoneLinkNetworkHandler.Frequency secondFreq = RedstoneLinkNetworkHandler.Frequency.of(second);
            String assumedModuleName = name.lastIndexOf('-') == -1 ? name : name.substring(0, name.lastIndexOf('-')-1);
            Optional<String> extension = name.lastIndexOf('-') >= 0 ? Optional.of(name.substring(name.lastIndexOf('-')+1)) : Optional.empty();
            return new ModuleEntry(firstFreq, secondFreq, new IOEntry(assumedModuleName, null, extension), null);
        }

        private void setEntryIfNull(Module mod) {
            IOEntry newEntry = IOEntry.newEntryIfTypeNull(this.entry, mod);
            if (newEntry == null) {
                this.parentalBE.getModuleEntries().set(this.entry, null);
                this.parentalBE.getModuleEntries().set(this.entry = newEntry, this);
                this.parentalBE.setChanged();
            }
        }

        private int sending = -1;
        @Override
        public int getTransmittedStrength() {
            return sending == -1 ? update() : sending;
        }
        private int update() {
            if (this.parentalBE == null) return 0;
            Module mod = this.parentalBE.getOrCreate().getCompiledModules().get(this.entry.name());
            if(mod == null) return 0;
            this.setEntryIfNull(mod);

            if (this.entry.type() == ModuleIOType.INPUT && mod instanceof IInput input) {
                return sending = Math.clamp(input.getAnalog(), 0, 15);
            } else if (this.entry.type() == ModuleIOType.MULTI_INPUT && mod instanceof IMultiInput input) {
                IMultiInput.AnalogResult[] result = new IMultiInput.AnalogResult[1];
                String ext = this.entry.extension().orElse("");
                input.getValues((k, v) -> {
                    if (Objects.equals(k, ext)) result[0] = v;
                });
                if (result != null) return sending = Math.clamp(result[0].getAnalog(), 0, 15);
            }
            return 0;
        }

        private int prevRecv = -1;
        @Override
        public void setReceivedStrength(int power) {
            if (power == prevRecv || this.parentalBE == null) return;
            Module mod = this.parentalBE.getOrCreate().getCompiledModules().get(this.entry.name());
            if(mod == null) return;
            this.setEntryIfNull(mod);

            if (this.entry.type() == ModuleIOType.OUTPUT && mod instanceof IOutput output) {
                output.setAnalog(power);
            } else if (this.entry.type() == ModuleIOType.MULTI_OUTPUT && mod instanceof IMultiOutput output) {
                IMultiOutput.AnalogRunnable[] runnable = new IMultiOutput.AnalogRunnable[1];
                String ext = this.entry.extension().orElse("");
                output.setValues((k, v) -> {
                    if (Objects.equals(k, ext)) v.setAnalog(power);
                });
            }
            mod.parentBlockEntity.blockChanged();
            this.prevRecv = power;
        }

        @Override
        public boolean isListening() {
            if (this.parentalBE == null) return false;
            return this.entry.type() == ModuleIOType.OUTPUT || this.entry.type() == ModuleIOType.MULTI_OUTPUT;
        }

        @Override
        public boolean isAlive() {
            return this.parentalBE != null;
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

        public IOEntry getEntry() {
            return this.entry;
        }

        public void setPos(BlockPos pos) {
            this.pos = pos;
        }

        public void setBe(PanelLinkBlockEntity be) {
            this.parentalBE = be;
        }
    }
}
