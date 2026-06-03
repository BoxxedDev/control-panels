package moth.boxxed.panels.datagen;

import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.index.PanelBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PanelLootProvider extends LootTableProvider {
    public PanelLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(
                        Block::new,
                        LootContextParamSets.BLOCK
                )
        ), registries);
    }

    public static class Block extends BlockLootSubProvider {
        protected Block(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
        }

        @Override
        protected Iterable<net.minecraft.world.level.block.Block> getKnownBlocks() {
            return PanelBlocks.BLOCKS.getEntries().stream().map(e -> (net.minecraft.world.level.block.Block)e.value()).toList();
        }

        @Override
        protected void generate() {
            dropSelf(PanelBlocks.CABLE.get());
            dropSelf(PanelBlocks.CONTROL_PANEL.get());
            dropOther(PanelBlocks.STRIPPED_CABLE.get(), PanelBlocks.CABLE.get());
            if (ModList.get().isLoaded("create"))
                dropSelf(PanelCreateRegistries.PANEL_LINK.get());
        }
    }
}
