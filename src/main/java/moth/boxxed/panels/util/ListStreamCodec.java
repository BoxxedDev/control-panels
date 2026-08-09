package moth.boxxed.panels.util;

import io.netty.buffer.ByteBuf;
import moth.boxxed.panels.api.module.io.IOEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record ListStreamCodec<BUF extends ByteBuf, C>(StreamCodec<BUF, C> codec) implements StreamCodec<BUF, List<C>> {
    @Override
    public List<C> decode(BUF buffer) {
        List<C> ret = new ArrayList<>();

        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            ret.add(this.codec.decode(buffer));
        }
        return ret;
    }

    @Override
    public void encode(BUF buffer, List<C> list) {
        buffer.writeInt(list.size());

        for (C c : list) {
            this.codec.encode(buffer, c);
        }
    }

    public static <BUF extends ByteBuf, C> ListStreamCodec<BUF, C> of(StreamCodec<BUF, C> codec) {
        return new ListStreamCodec<>(codec);
    }
}
