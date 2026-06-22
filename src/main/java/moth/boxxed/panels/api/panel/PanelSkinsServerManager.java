package moth.boxxed.panels.api.panel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import moth.boxxed.panels.Dashpanels;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelSkinsServerManager {
    public static final Map<PanelType, ServerSkin> MAP = new HashMap<>();

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public static final ReloadListener INSTANCE = new ReloadListener(
                new GsonBuilder().setLenient().create(),
                "panel_skins"
        );

        public ReloadListener(Gson gson, String directory) {
            super(gson, directory);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> locationElementMap, ResourceManager resourceManager, ProfilerFiller profiler) {
            PanelSkinsServerManager.MAP.clear();
            int loaded = 0;
            for (ResourceLocation location : locationElementMap.keySet()) {
                JsonElement element = locationElementMap.get(location);
                if (element instanceof JsonObject object) {
                    DataResult<ServerSkin> result = ServerSkin.CODEC.parse(JsonOps.INSTANCE, object);
                    if (result.isSuccess()) {
                        ServerSkin serverSkin = result.getOrThrow();
                        PanelSkinsServerManager.MAP.compute(serverSkin.type(),
                                (type, skin) -> skin == null ? serverSkin : skin.putSkins(serverSkin)
                        );
                        loaded += serverSkin.skins().size();
                        continue;
                    }
                }
                Dashpanels.LOGGER.error("Could not parse server skin file at : {}", location);
            }
            Dashpanels.LOGGER.info("Loaded {} server skins", loaded);
        }
    }
}