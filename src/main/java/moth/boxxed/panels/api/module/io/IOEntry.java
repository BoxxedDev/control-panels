package moth.boxxed.panels.api.module.io;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import moth.boxxed.panels.api.module.Module;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record IOEntry(String name, ModuleIOType type, Optional<String> extension) {
    public static final StreamCodec<RegistryFriendlyByteBuf, IOEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, IOEntry::name,
            ModuleIOType.STREAM_CODEC, IOEntry::type,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), IOEntry::extension,
            IOEntry::new
    );

    public static final Codec<IOEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(IOEntry::name),
            ModuleIOType.CODEC.fieldOf("type").forGetter(IOEntry::type),
            Codec.STRING.optionalFieldOf("extension").forGetter(IOEntry::extension)
    ).apply(instance, IOEntry::new));

    public static IOEntry newEntryIfTypeNull(IOEntry old, Module module) {
        if (old == null)
            return null;

        //Check so like it doesn't break existing stuff yk?
        if (old.type() == null) {
            ModuleIOType ioType = null;
            if (old.extension().isPresent()) {
                List<String> inputs = new ArrayList<>();
                List<String> outputs = new ArrayList<>();
                if (module instanceof IMultiInput multiInput) {
                    multiInput.getValues((str, e) -> inputs.add(str));
                } else if (module instanceof IMultiOutput multiOutput) {
                    multiOutput.setValues((str, e) -> outputs.add(str));
                }
                String extension = old.extension().get();
                if (inputs.contains(extension)) {
                    ioType = ModuleIOType.MULTI_INPUT;
                } else if (outputs.contains(extension)) {
                    ioType = ModuleIOType.MULTI_OUTPUT;
                }
            } else {
                if (module instanceof IInput) {
                    ioType = ModuleIOType.INPUT;
                } else if (module instanceof IOutput) {
                    ioType = ModuleIOType.OUTPUT;
                }
            }
            if (ioType != null) {
                return new IOEntry(old.name(), ioType, old.extension());
            }
        } else {
            return old;
        }
        return null;
    }

    @Override
    public @NonNull String toString() {
        return this.extension.map(string -> "%s - %s".formatted(this.name, string)).orElse(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IOEntry(String otherName, ModuleIOType otherType, Optional<String> otherExtension)) {
            return Objects.equals(this.name, otherName) && this.type == otherType && this.extension.equals(otherExtension);
        }
        return false;
    }

    public static IOEntry fromTag(Tag tag, HolderLookup.Provider registries) {
        try {
            RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            DataResult<Pair<IOEntry, Tag>> entryDataResult = CODEC.decode(ops, tag);
            if (entryDataResult.isSuccess()) {
                return entryDataResult.getOrThrow().getFirst();
            }
        } catch (IllegalStateException e) {}
        return null;
    }

    public Tag asTag(HolderLookup.Provider registries) {
        try {
            RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> result = CODEC.encodeStart(ops, this);
            return result.getOrThrow();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
