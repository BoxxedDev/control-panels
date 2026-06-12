package moth.boxxed.panels.api.module;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ModuleIOType implements StringRepresentable {
    INPUT,
    OUTPUT,
    MULTI_INPUT,
    MULTI_OUTPUT;

    public static final StringRepresentable.EnumCodec<ModuleIOType> CODEC = StringRepresentable.fromEnum(ModuleIOType::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleIOType> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static ModuleIOType decide(Module value) {
        if (value instanceof IInput)
            return INPUT;
        if (value instanceof IOutput)
            return OUTPUT;
        if (value instanceof IMultiInput)
            return MULTI_INPUT;
        if (value instanceof IMultiOutput)
            return MULTI_OUTPUT;
        return null;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
