package moth.boxxed.panels.datagen;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class PanelRecipeProvider extends RecipeProvider {
    public PanelRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        controlPanel().save(output);
        cable().save(output);
        wire().save(output);
        wireStripper().save(output);
        if (ModList.get().isLoaded("create"))
            panelLink().save(output);
    }

    private static ShapedRecipeBuilder panelLink() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelCreateRegistries.PANEL_LINK.get(), 1)
                .define('L', AllBlocks.REDSTONE_LINK)
                .define('T', AllItems.TRANSMITTER)
                .define('C', PanelBlocks.CABLE)
                .pattern("TLT")
                .pattern("CCC")
                .unlockedBy("has_link", has(AllBlocks.REDSTONE_LINK));
    }

    private static ShapedRecipeBuilder controlPanel() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelBlocks.CONTROL_PANEL.get(), 1)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Items.REDSTONE)
                .define('P', ItemTags.PLANKS)
                .pattern("I  ")
                .pattern("IPP")
                .pattern("IRI")
                .unlockedBy("has_redstone", has(Items.REDSTONE));
    }
    private static ShapedRecipeBuilder cable() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelBlocks.CABLE.get(), 6)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Items.DRIED_KELP)
                .define('C', PanelItems.COPPER_WIRE)
                .pattern("RRR")
                .pattern("ICI")
                .unlockedBy("has_wire", has(PanelItems.COPPER_WIRE));
    }
    private static ShapedRecipeBuilder wire() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelItems.COPPER_WIRE.get(), 4)
                .define('C', Tags.Items.INGOTS_COPPER)
                .pattern("CC")
                .unlockedBy("has_copper", has(Items.COPPER_INGOT));
    }
    private static ShapedRecipeBuilder wireStripper() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelItems.CABLE_STRIPPER.get(), 1)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('H', ItemTags.WOOL)
                .pattern("I I")
                .pattern(" I ")
                .pattern("H H")
                .unlockedBy("has_wool", has(ItemTags.WOOL));
    }
}
