package moth.boxxed.panels.datagen;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.index.PanelBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Function;

public class PanelModelProviders {
    public static class Item extends ItemModelProvider {
        public Item(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, ControlPanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            simpleBlockItem(PanelBlocks.CONTROL_PANEL.get());
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
