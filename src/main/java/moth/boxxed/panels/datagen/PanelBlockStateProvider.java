package moth.boxxed.panels.datagen;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.index.PanelBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class PanelBlockStateProvider extends BlockStateProvider {
    public PanelBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ControlPanels.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(PanelBlocks.CONTROL_PANEL.get(), models().getExistingFile(ControlPanels.path("control_panel")));
    }
}
