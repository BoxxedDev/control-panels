package moth.boxxed.panels.datagen;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.index.PanelBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class PanelBlockStateProvider extends BlockStateProvider {
    public PanelBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ControlPanels.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(PanelBlocks.CONTROL_PANEL.get(), models().getExistingFile(ControlPanels.path("control_panel")));

        cable();
    }

    private void cable() {

        MultiPartGenerator generator = MultiPartGenerator.multiPart(PanelBlocks.CABLE.get());

        ModelFile.ExistingModelFile coreFile = models().getExistingFile(ControlPanels.path("block/cable/core"));
        ModelFile.ExistingModelFile facingFile = models().getExistingFile(ControlPanels.path("block/cable/facing"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(PanelBlocks.CABLE.get());
        builder.part()
                .modelFile(coreFile)
                .addModel()
                .condition(CableBlock.NORTH, true, false)
                .condition(CableBlock.SOUTH, true, false)
                .condition(CableBlock.EAST, true, false)
                .condition(CableBlock.WEST, true, false)
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
