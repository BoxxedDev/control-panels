package moth.boxxed.panels.util;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import moth.boxxed.panels.api.module.io.ModuleIOType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class StreamCodecUtil {
    public static <BUF extends ByteBuf, A, B> StreamCodec<BUF, Pair<A, B>> pair(StreamCodec<BUF, A> codecA, StreamCodec<BUF, B> codecB) {
        return new StreamCodec<>() {
            @Override
            public Pair<A, B> decode(BUF buffer) {
                return new Pair<>(
                        codecA.decode(buffer),
                        codecB.decode(buffer)
                );
            }

            @Override
            public void encode(BUF buffer, Pair<A, B> value) {
                codecA.encode(buffer, value.getFirst());
                codecB.encode(buffer, value.getSecond());
            }
        };
    }
}
