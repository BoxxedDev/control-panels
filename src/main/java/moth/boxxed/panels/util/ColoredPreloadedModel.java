package moth.boxxed.panels.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class ColoredPreloadedModel {
    public Map<DyeColor, PreLoadedModel> models = new HashMap<>();

    public ColoredPreloadedModel(ResourceLocation modelLocation) {
        for (DyeColor color : DyeColor.values()) {
            String extension = "_%s".formatted(color.getSerializedName());
            models.put(color, PreLoadedModel.create(modelLocation.withSuffix(extension)));
        }
    }

    public PreLoadedModel getModel(DyeColor color) {
        return models.get(color);
    }
}
