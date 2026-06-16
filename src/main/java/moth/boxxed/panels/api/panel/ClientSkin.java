package moth.boxxed.panels.api.panel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import moth.boxxed.panels.Dashpanels;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record ClientSkin(ResourceLocation single,
                         ResourceLocation left,
                         ResourceLocation center,
                         ResourceLocation right) {
    public static final ResourceLocation DEFAULT_SINGLE = Dashpanels.path("block/control_panel/single");
    public static final ResourceLocation DEFAULT_LEFT = Dashpanels.path("block/control_panel/left");
    public static final ResourceLocation DEFAULT_CENTER = Dashpanels.path("block/control_panel/center");
    public static final ResourceLocation DEFAULT_RIGHT = Dashpanels.path("block/control_panel/right");

    public static final Codec<ClientSkin> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("single").orElse(DEFAULT_SINGLE).forGetter(ClientSkin::single),
                    ResourceLocation.CODEC.fieldOf("left").orElse(DEFAULT_LEFT).forGetter(ClientSkin::left),
                    ResourceLocation.CODEC.fieldOf("center").orElse(DEFAULT_CENTER).forGetter(ClientSkin::center),
                    ResourceLocation.CODEC.fieldOf("right").orElse(DEFAULT_RIGHT).forGetter(ClientSkin::right)
            ).apply(instance, ClientSkin::new)
    );

    private List<ResourceLocation> compileLocations() {
        List<ResourceLocation> locations = new ArrayList<>();
        locations.add(this.single);
        locations.add(this.left);
        locations.add(this.center);
        locations.add(this.right);
        return locations;
    }

    public void registerModels(ModelBakery bakery) {
        for (ResourceLocation location : compileLocations()) {
            UnbakedModel unbakedModel = bakery.getModel(location);
            bakery.registerModel(ModelResourceLocation.standalone(location), unbakedModel);
        }
    }
}