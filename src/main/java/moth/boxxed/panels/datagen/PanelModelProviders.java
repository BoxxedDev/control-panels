package moth.boxxed.panels.datagen;

import moth.boxxed.panels.ControlPanels;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class PanelModelProviders {
    public static class Item extends ItemModelProvider {
        public Item(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, ControlPanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            withExistingParent("control_panel", ControlPanels.path("block/control_panel/single"));
        }
    }

    public static class Block extends BlockModelProvider {
        public Block(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, ControlPanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
        }
    }
}
