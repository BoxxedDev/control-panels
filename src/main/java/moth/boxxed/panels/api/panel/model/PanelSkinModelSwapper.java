package moth.boxxed.panels.api.panel.model;

import moth.boxxed.panels.index.PanelBlocks;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelSkinModelSwapper {
    public static final PanelSkinModelSwapper INSTANCE = new PanelSkinModelSwapper();

    public void modifyResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> blocks = event.getModels();
        getBlockStateModels(PanelBlocks.CONTROL_PANEL.get()).forEach(modelResourceLocation -> {
            blocks.put(modelResourceLocation, new PanelSkinModel(blocks.get(modelResourceLocation)));
        });
    }

    public static List<ModelResourceLocation> getBlockStateModels(Block block) {
        List<ModelResourceLocation> models = new ArrayList<>();
        ResourceLocation blockRl = RegisteredObjectsHelper.getKeyOrThrow(block);
        block.getStateDefinition()
                .getPossibleStates()
                .forEach(state -> {
                    models.add(BlockModelShaper.stateToModelLocation(blockRl, state));
                });
        return models;
    }
}
