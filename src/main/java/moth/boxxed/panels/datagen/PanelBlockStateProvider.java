package moth.boxxed.panels.datagen;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.index.PanelBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class PanelBlockStateProvider extends BlockStateProvider {
    public PanelBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Dashpanels.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        controlPanel();
        cable();
        horizontalBlock(PanelBlocks.STRIPPED_CABLE.get(), models().getExistingFile(Dashpanels.path("block/cable/stripped")));
        panelLink();
    }

    private void controlPanel() {
        ModelFile single = models().getExistingFile(Dashpanels.path("block/control_panel/single"));
        ModelFile center = models().getExistingFile(Dashpanels.path("block/control_panel/center"));
        ModelFile left = models().getExistingFile(Dashpanels.path("block/control_panel/left"));
        ModelFile right = models().getExistingFile(Dashpanels.path("block/control_panel/right"));

        horizontalBlock(PanelBlocks.CONTROL_PANEL.get(), (blockState) ->
                switch (blockState.getValue(PanelBlock.SHAPE)) {
                    case SINGLE -> single;
                    case LEFT -> left;
                    case CENTER -> center;
                    case RIGHT -> right;
                });
    }

    private void panelLink() {
        if (ModList.get().isLoaded("create")) {
            ModelFile.ExistingModelFile facingFile = models().getExistingFile(Dashpanels.path("block/cable/facing"));
            ModelFile.ExistingModelFile linkMain = models().getExistingFile(Dashpanels.path("block/control_link"));

            MultiPartBlockStateBuilder builder = getMultipartBuilder(PanelCreateRegistries.PANEL_LINK.get());
            builder.part()
                    .modelFile(linkMain)
                    .addModel()
                    .end();
            builder.part()
                    .modelFile(facingFile)
                    .addModel()
                    .condition(CableBlock.NORTH, true)
                    .end();
            builder.part()
                    .modelFile(facingFile)
                    .rotationY(90)
                    .addModel()
                    .condition(CableBlock.EAST, true)
                    .end();
            builder.part()
                    .modelFile(facingFile)
                    .rotationY(180)
                    .addModel()
                    .condition(CableBlock.SOUTH, true)
                    .end();
            builder.part()
                    .modelFile(facingFile)
                    .rotationY(270)
                    .addModel()
                    .condition(CableBlock.WEST, true)
                    .end();
        }
    }

    private void cable() {
        ModelFile.ExistingModelFile coreFile = models().getExistingFile(Dashpanels.path("block/cable/core"));
        ModelFile.ExistingModelFile facingFile = models().getExistingFile(Dashpanels.path("block/cable/facing"));
        ModelFile.ExistingModelFile topFile = models().getExistingFile(Dashpanels.path("block/cable/top"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(PanelBlocks.CABLE.get());
        builder.part()
                .modelFile(coreFile)
                .addModel()
                .end();
        builder.part()
                .modelFile(facingFile)
                .addModel()
                .condition(CableBlock.NORTH, true)
                .end();
        builder.part()
                .modelFile(facingFile)
                .rotationY(90)
                .addModel()
                .condition(CableBlock.EAST, true)
                .end();
        builder.part()
                .modelFile(facingFile)
                .rotationY(180)
                .addModel()
                .condition(CableBlock.SOUTH, true)
                .end();
        builder.part()
                .modelFile(facingFile)
                .rotationY(270)
                .addModel()
                .condition(CableBlock.WEST, true)
                .end();
        builder.part()
                .modelFile(topFile)
                .addModel()
                .condition(CableBlock.UP, true)
                .end();
    }
}
