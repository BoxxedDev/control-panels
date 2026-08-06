package moth.boxxed.panels.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record EnumStreamCodec<T extends Enum<T>>(Class<T> clazz) implements StreamCodec<FriendlyByteBuf, T> {
    @Override
    public T decode(FriendlyByteBuf buffer) {
        return buffer.readEnum(this.clazz);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, T value) {
        buffer.writeEnum(value);
    }
}
