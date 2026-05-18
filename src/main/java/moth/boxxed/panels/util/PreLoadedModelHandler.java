package moth.boxxed.panels.util;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

public class PreLoadedModelHandler {
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation location : PreLoadedModel.ALL_MODELS.keySet()) {
            event.register(ModelResourceLocation.standalone(location));
        }
    }

    public static void bakingCompleted(ModelEvent.BakingCompleted event) {
        for (Map.Entry<ResourceLocation, PreLoadedModel> entry : PreLoadedModel.ALL_MODELS.entrySet()) {
            entry.getValue().model = event.getModels().get(ModelResourceLocation.standalone(entry.getKey()));
        }
    }
}
