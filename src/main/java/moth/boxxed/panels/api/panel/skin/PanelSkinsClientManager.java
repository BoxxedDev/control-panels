package moth.boxxed.panels.api.panel.skin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import moth.boxxed.panels.Dashpanels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class PanelSkinsClientManager {
    public static final Map<ResourceLocation, ClientSkin> MAP = new HashMap<>();

    public static class ReloadListener extends ContextAwareReloadListener implements PreparableReloadListener {
        public static final ReloadListener INSTANCE = new ReloadListener(
                new GsonBuilder().setLenient().create(),
                "panel_skins"
        );

        private final Gson gson;
        private final String directory;

        public ReloadListener(Gson gson, String directory) {
            this.gson = gson;
            this.directory = directory;
        }

        @Override
        public CompletableFuture<Void> reload(
                PreparationBarrier stage,
                ResourceManager resourceManager,
                ProfilerFiller preparationsProfiler,
                ProfilerFiller reloadProfiler,
                Executor backgroundExecutor,
                Executor gameExecutor) {
            return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager, preparationsProfiler), backgroundExecutor)
                    .thenCompose(stage::wait)
                    .thenAcceptAsync(map -> this.apply(map, resourceManager, reloadProfiler), gameExecutor)
                    .thenAccept(map -> this.rebakeModels(resourceManager, backgroundExecutor));
        }

        private void rebakeModels(ResourceManager resourceManager, Executor backgroundExecutor) {
            Multimap<ModelResourceLocation, Material> multimap = HashMultimap.create();
            ModelManager manager = Minecraft.getInstance().getModelManager();
            Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> map = manager.atlases.scheduleLoad(resourceManager, manager.maxMipmapLevels, backgroundExecutor);
            Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, p_248988_ -> p_248988_.getValue().join()));
            Minecraft.getInstance().getModelManager().getModelBakery().bakeModels((p_352403_, p_251262_) -> {
                AtlasSet.StitchResult atlasset$stitchresult = atlasPreparations.get(p_251262_.atlasLocation());
                TextureAtlasSprite textureatlassprite = atlasset$stitchresult.getSprite(p_251262_.texture());
                if (textureatlassprite != null) {
                    return textureatlassprite;
                } else {
                    multimap.put(p_352403_, p_251262_);
                    return atlasset$stitchresult.missing();
                }
            });
        }

        protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            Map<ResourceLocation, JsonElement> map = new HashMap<>();
            scanDirectory(resourceManager, this.directory, this.gson, map);
            return map;
        }

        public static void scanDirectory(ResourceManager resourceManager, String name, Gson gson, Map<ResourceLocation, JsonElement> output) {
            FileToIdConverter filetoidconverter = FileToIdConverter.json(name);

            for (Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(resourceManager).entrySet()) {
                ResourceLocation resourcelocation = entry.getKey();
                ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonElement jsonelement = GsonHelper.fromJson(gson, reader, JsonElement.class);
                    JsonElement jsonelement1 = output.put(resourcelocation1, jsonelement);
                    if (jsonelement1 != null) {
                        throw new IllegalStateException("Duplicate data file ignored with ID: " + resourcelocation1);
                    }
                } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                    Dashpanels.LOGGER.error("Couldn't parse data file {} from {}", resourcelocation1, resourcelocation, jsonparseexception);
                }
            }
        }

        protected void apply(Map<ResourceLocation, JsonElement> elementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            PanelSkinsClientManager.MAP.clear();
            int loaded = 0;

            ModelBakery bakery = Minecraft.getInstance().getModelManager().getModelBakery();
            for (ResourceLocation location : elementMap.keySet()) {
                JsonElement element = elementMap.get(location);
                if (element instanceof JsonObject object) {
                    DataResult<ClientSkin> result = ClientSkin.CODEC.parse(JsonOps.INSTANCE, object);
                    if (result.isSuccess()) {
                        ClientSkin clientSkin = result.getOrThrow();
                        PanelSkinsClientManager.MAP.put(location, clientSkin);
                        clientSkin.registerModels(bakery);
                        loaded++;
                        continue;
                    }
                }
                Dashpanels.LOGGER.error("Could not parse client skin file at : {}", location);
            }
            Dashpanels.LOGGER.info("Loaded {} client skins", loaded);
        }
    }
}