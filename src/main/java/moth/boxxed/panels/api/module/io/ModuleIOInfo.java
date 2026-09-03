package moth.boxxed.panels.api.module.io;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.util.ListStreamCodec;
import moth.boxxed.panels.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//The pair left will be input so multi input and just input, right will be output.
public record ModuleIOInfo(String name, Either<ModuleIOType, Pair<ModuleIOType, ModuleIOType>> type, List<IOEntry> ioEntries) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleIOInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ModuleIOInfo::name,
            ByteBufCodecs.either(
                    ModuleIOType.STREAM_CODEC,
                    StreamCodecUtil.pair(
                            ModuleIOType.STREAM_CODEC,
                            ModuleIOType.STREAM_CODEC
                    )
            ), ModuleIOInfo::type,
            ListStreamCodec.of(IOEntry.STREAM_CODEC), ModuleIOInfo::ioEntries,
            ModuleIOInfo::new
    );

    public static List<IOEntry> compileIOEntries(Module value) {
        List<IOEntry> toReturn = new ArrayList<>();
        if (value instanceof IInput)
            toReturn.add(new IOEntry(
                    value.getName(),
                    ModuleIOType.INPUT,
                    Optional.empty()
            ));
        if (value instanceof IOutput)
            toReturn.add(new IOEntry(
                    value.getName(),
                    ModuleIOType.OUTPUT,
                    Optional.empty()
            ));
        if (value instanceof IMultiInput multiInput)
            multiInput.getValues((extension, result) -> toReturn.add(
                        new IOEntry(value.getName(), ModuleIOType.MULTI_INPUT, Optional.of(extension))
                ));
        if (value instanceof IMultiOutput multiOutput)
            multiOutput.setValues((extension, runnable) -> toReturn.add(
                    new IOEntry(value.getName(), ModuleIOType.MULTI_OUTPUT, Optional.of(extension))
            ));
        return toReturn;
    }

    public static ModuleIOInfo create(String name, Module module) {
        if (!(module instanceof IInput || module instanceof IMultiInput ||
                module instanceof IOutput || module instanceof IMultiOutput)) {
            return null;
        }

        boolean isBothTypes = (module instanceof IInput ^ module instanceof IMultiInput) && (module instanceof IOutput ^ module instanceof IMultiOutput);
        if (isBothTypes) {
            ModuleIOType left = module instanceof IInput ? ModuleIOType.INPUT : ModuleIOType.MULTI_INPUT;
            ModuleIOType right = module instanceof IOutput ? ModuleIOType.OUTPUT : ModuleIOType.MULTI_OUTPUT;
            return new ModuleIOInfo(
                    name,
                    Either.right(new Pair<>(left, right)),
                    compileIOEntries(module)
            );
        } else {
            return new ModuleIOInfo(
                    name,
                    Either.left(ModuleIOType.decide(module)),
                    compileIOEntries(module)
            );
        }
    }
}
