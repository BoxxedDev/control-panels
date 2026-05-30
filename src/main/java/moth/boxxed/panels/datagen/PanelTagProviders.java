package moth.boxxed.panels.datagen;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.index.PanelTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PanelTagProviders {
    public static class Items extends TagsProvider<net.minecraft.world.item.Item> {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, Registries.ITEM, lookupProvider, ControlPanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(PanelTags.Items.MODULE)
                    .add(PanelItems.SWITCH_MODULE.getKey())
                    .add(PanelItems.KNOB_MODULE.getKey())
                    .add(PanelItems.CONTROL_LEVER_MODULE.getKey());
        }
    }

    public static class Blocks extends TagsProvider<Block> {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, Registries.BLOCK, lookupProvider, ControlPanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(PanelBlocks.CONTROL_PANEL.getKey())
                    .add(PanelBlocks.CABLE.getKey())
                    .add(PanelBlocks.STRIPPED_CABLE.getKey());
        }
    }
}
