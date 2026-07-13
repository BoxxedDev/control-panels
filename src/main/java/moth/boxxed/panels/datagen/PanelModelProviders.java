package moth.boxxed.panels.datagen;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class PanelModelProviders {
    public static class Item extends ItemModelProvider {
        public Item(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Dashpanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            withExistingParent("control_panel", Dashpanels.path("block/control_panel/single"));
            withExistingParent("wall_control_panel", Dashpanels.path("block/wall_control_panel/single"));
            withExistingParent("panel_link", Dashpanels.path("block/control_link"));
            basicItem(PanelItems.CABLE_STRIPPER.get());
            basicItem(PanelItems.PAINT_BRUSH.get());
        }
    }

    public static class Block extends BlockModelProvider {
        public Block(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Dashpanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
        }
    }
}
