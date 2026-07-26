package moth.boxxed.panels.api.panel.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ClientSkin(ResourceLocation single,
                         ResourceLocation left,
                         ResourceLocation center,
                         ResourceLocation right,
                         Optional<Boolean> tintable) {
    private static final ResourceLocation DEFAULT_SINGLE = Dashpanels.path("block/control_panel/single");
    private static final ResourceLocation DEFAULT_LEFT = Dashpanels.path("block/control_panel/left");
    private static final ResourceLocation DEFAULT_CENTER = Dashpanels.path("block/control_panel/center");
    private static final ResourceLocation DEFAULT_RIGHT = Dashpanels.path("block/control_panel/right");
    public static final ClientSkin DEFAULT = new ClientSkin(
            DEFAULT_SINGLE,
            DEFAULT_LEFT,
            DEFAULT_CENTER,
            DEFAULT_RIGHT,
            Optional.of(false)
    );

    public static final Codec<ClientSkin> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("single").orElse(DEFAULT_SINGLE).forGetter(ClientSkin::single),
                    ResourceLocation.CODEC.fieldOf("left").orElse(DEFAULT_LEFT).forGetter(ClientSkin::left),
                    ResourceLocation.CODEC.fieldOf("center").orElse(DEFAULT_CENTER).forGetter(ClientSkin::center),
                    ResourceLocation.CODEC.fieldOf("right").orElse(DEFAULT_RIGHT).forGetter(ClientSkin::right),
                    Codec.BOOL.optionalFieldOf("tintable").forGetter(ClientSkin::tintable)
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

    public BakedModel getItemBakedModel() {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.single));
    }

    public BakedModel getBlockModel(AbstractPanelBlock.Shape shape) {
        if (shape == null)
            return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.single));

        return switch (shape) {
            case SINGLE -> Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.single));
            case LEFT -> Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.left));
            case CENTER -> Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.center));
            case RIGHT -> Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.right));
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==this) {
            return true;
        } else {
            if (!(obj instanceof ClientSkin other))
                return false;

            return this.single.equals(other.single) &&
                    this.left.equals(other.left) &&
                    this.center.equals(other.center) &&
                    this.right.equals(other.right);
        }
    }
}