package moth.boxxed.panels.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ListStreamCodec<C>(StreamCodec<ByteBuf, C> codec) implements StreamCodec<FriendlyByteBuf, List<C>> {
    @Override
    public List<C> decode(FriendlyByteBuf buffer) {
        return buffer.readList(this.codec);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, List<C> value) {
        buffer.writeCollection(value, this.codec);
    }
}
