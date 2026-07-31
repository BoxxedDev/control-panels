package moth.boxxed.panels.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

//Tbh I just made this because I see more modules using some kind of ID system in the future and the string version of UUID is too big
public class ShortUUID {
    public static final Codec<ShortUUID> CODEC = Codec.INT
            .comapFlatMap(ShortUUID::resultFromInt, ShortUUID::getBits);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShortUUID> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull ShortUUID decode(RegistryFriendlyByteBuf buffer) {return ShortUUID.fromInt(buffer.readInt());}
        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ShortUUID value) {buffer.writeInt(value.bits);}
    };

    private static final SecureRandom numberGenerator = new SecureRandom();

    private final int bits;

    public ShortUUID(byte[] data) {
        assert data.length == 4 : "data must be 4 bytes in length";
        int b = 0;
        for (int i = 0; i < 4; i++) {
            b = (b << 8) | (data[i] & 0xFF);
        }
        this.bits = b;
    }

    public int getBits() {
        return this.bits;
    }

    public static ShortUUID random() {
        byte[] randomBytes = new byte[4];
        numberGenerator.nextBytes(randomBytes);
        return new ShortUUID(randomBytes);
    }

    public static ShortUUID fromInt(int integer) {
        return new ShortUUID(ByteBuffer.allocate(4).putInt(integer).array());
    }

    private static DataResult<ShortUUID> resultFromInt(int integer) {
        return DataResult.success(fromInt(integer));
    }

    @Override
    public String toString() {
        String a = Integer.toHexString((bits >> 16) & 0xFFFF);
        String b = Integer.toHexString(bits & 0xFFFF);

        return a + '-' + b;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShortUUID shortUUID) {
            return this.bits == shortUUID.bits;
        }
        return false;
    }

    public void put(String id, CompoundTag tag) {
        tag.putInt(id, this.bits);
    }

    public static ShortUUID fromTag(String id, CompoundTag tag) {
        return ShortUUID.fromInt(tag.getInt(id));
    }
}
