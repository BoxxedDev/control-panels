package moth.boxxed.panels.network.packet.name;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum NameType implements StringRepresentable {
    VALIDATE,
    GENERATE;

    public static final StreamCodec<RegistryFriendlyByteBuf, NameType> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, NameType>() {
        @Override
        public NameType decode(RegistryFriendlyByteBuf buffer) {
            return NameType.valueOf(Utf8String.read(buffer, 8));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, NameType value) {
            Utf8String.write(buffer, value.getSerializedName(), 8);
        }
    };

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
