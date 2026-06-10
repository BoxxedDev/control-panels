package moth.boxxed.panels.datagen;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.compat.sable.PanelSableRegistries;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.index.PanelTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PanelTagProviders {
    public static class Items extends TagsProvider<net.minecraft.world.item.Item> {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, Registries.ITEM, lookupProvider, Dashpanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(PanelTags.Items.MODULE)
                    .add(PanelItems.SWITCH_MODULE.getKey())
                    .add(PanelItems.KNOB_MODULE.getKey())
                    .add(PanelItems.CONTROL_LEVER_MODULE.getKey())
                    .add(PanelItems.INDICATOR_BULB_MODULE.getKey())
                    .add(PanelItems.MOMENTARY_SWITCH_MODULE.getKey())
                    .add(PanelItems.JOYSTICK_MODULE.getKey())
                    .add(PanelItems.LABEL_MODULE.getKey())
            ;
            if (ModList.get().isLoaded("sable"))
                tag(PanelTags.Items.MODULE)
                        .add(PanelSableRegistries.NAVBALL_MODULE.getKey());
        }
    }

    public static class Blocks extends TagsProvider<Block> {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, Registries.BLOCK, lookupProvider, Dashpanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(PanelBlocks.CONTROL_PANEL.getKey())
                    .add(PanelBlocks.CABLE.getKey())
                    .add(PanelBlocks.STRIPPED_CABLE.getKey());
            if (ModList.get().isLoaded("create")) {
                tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .add(PanelCreateRegistries.PANEL_LINK.getKey());
                tag(BlockTags.MINEABLE_WITH_AXE)
                        .add(PanelCreateRegistries.PANEL_LINK.getKey());
            }
        }
    }
}
