package moth.boxxed.panels.api.panel.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.util.ListStreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ServerSkin(PanelType type, Set<ResourceLocation> skins) {
    public static final Codec<ServerSkin> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    PanelType.CODEC.fieldOf("panel").orElse(PanelType.DEFAULT).forGetter(ServerSkin::type),
                    new ListCodec<>(ResourceLocation.CODEC, 1, 512).fieldOf("skins").forGetter(ServerSkin::skinsList)
            ).apply(instance, ServerSkin::fromCodec)
    );
    public static final StreamCodec<FriendlyByteBuf, ServerSkin> STREAM_CODEC = StreamCodec.composite(
            PanelType.STREAM_CODEC, ServerSkin::type,
            new ListStreamCodec<>(ResourceLocation.STREAM_CODEC), ServerSkin::skinsList,
            ServerSkin::fromCodec
    );

    private static ServerSkin fromCodec(PanelType panelType, List<ResourceLocation> objects) {
        return new ServerSkin(panelType, new HashSet<>(objects));
    }

    public List<ResourceLocation> skinsList() {
        return new ArrayList<>(this.skins);
    }

    public ServerSkin putSkins(ServerSkin serverSkin) {
        this.skins.addAll(serverSkin.skins);
        return this;
    }
}
