package moth.boxxed.panels.content.modules.key_switch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import moth.boxxed.panels.util.ShortUUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BoundModule(BlockPos pos, ShortUUID uuid) {
    public static final Codec<BoundModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(BoundModule::pos),
                    ShortUUID.CODEC.fieldOf("uuid").forGetter(BoundModule::uuid)
            ).apply(instance, BoundModule::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BoundModule> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BoundModule::pos,
            ShortUUID.STREAM_CODEC, BoundModule::uuid,
            BoundModule::new
    );
}
