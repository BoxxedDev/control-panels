package moth.boxxed.panels.api.module.io;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record ModuleIOInfo(String name, ModuleIOType type, List<String> multiExtension) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleIOInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ModuleIOInfo::name,
            ModuleIOType.STREAM_CODEC, ModuleIOInfo::type,
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buffer, List<String> value) {buffer.writeCollection(value, ByteBufCodecs.STRING_UTF8);}
                @Override
                public List<String> decode(RegistryFriendlyByteBuf buffer) {return buffer.readCollection(ArrayList::new, ByteBufCodecs.STRING_UTF8);}
            }, ModuleIOInfo::multiExtension,
            ModuleIOInfo::new
    );

    public static List<String> getMultiExtensionsIfAny(Module value) {
        if (value instanceof IInput || value instanceof IOutput) return new ArrayList<>();

        List<String> toReturn = new ArrayList<>();
        if (value instanceof IMultiInput multiInput)
            multiInput.getValues((extension, result) -> toReturn.add(extension));
        if (value instanceof IMultiOutput multiOutput)
            multiOutput.setValues((extension, runnable) -> toReturn.add(extension));
        return toReturn;
    }
}
