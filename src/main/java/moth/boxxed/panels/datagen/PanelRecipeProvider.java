package moth.boxxed.panels.datagen;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.compat.sable.PanelSableRegistries;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;
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
        wireStripper().save(output);
        if (ModList.get().isLoaded("create"))
            panelLink().save(output);
        modules(output);
    }

    private static void modules(RecipeOutput output) {
        module(PanelItems.SWITCH_MODULE.get(), Ingredient.of(Tags.Items.INGOTS_IRON), 4, output);
        module(PanelItems.KNOB_MODULE.get(), Ingredient.of(Tags.Items.INGOTS_IRON), 4, output);
        module(PanelItems.CONTROL_LEVER_MODULE.get(), Ingredient.of(Tags.Items.INGOTS_IRON), 2, output);
        module(PanelItems.INDICATOR_BULB_MODULE.get(), Ingredient.of(Tags.Items.GLASS_BLOCKS), 4, output);
        module(PanelItems.MOMENTARY_SWITCH_MODULE.get(), Ingredient.of(Tags.Items.INGOTS_IRON), 4, output);
        module(PanelItems.JOYSTICK_MODULE.get(), Ingredient.of(Tags.Items.INGOTS_IRON), 2, output);
        module(PanelItems.LABEL_MODULE.get(), Ingredient.of(Items.PAPER), 4, output);
        if (ModList.get().isLoaded("sable"))
            module(PanelSableRegistries.NAVBALL_MODULE.get(), Ingredient.of(Tags.Items.INGOTS_IRON), 2, output);
    }

    private static void module(ItemLike item, Ingredient ingredient, int count, RecipeOutput output) {
        ResourceLocation id = RecipeBuilder.getDefaultRecipeId(item);
        Advancement.Builder builder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        StonecutterRecipe recipe = new StonecutterRecipe(
            "",
                ingredient,
                new ItemStack(item, count)
        );
        output.accept(
                id,
                recipe,
                builder.build(id.withPrefix("recipes/" + RecipeCategory.REDSTONE.getFolderName() + "/"))
        );
    }

    private static void modulesOld(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelItems.SWITCH_MODULE.get(), 4)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('N', Tags.Items.NUGGETS_IRON)
                .pattern("N")
                .pattern("I")
                .unlockedBy("has_panel", has(PanelBlocks.CONTROL_PANEL))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelItems.KNOB_MODULE.get(), 4)
                .define('W', ItemTags.WOOL)
                .define('I', Tags.Items.INGOTS_IRON)
                .pattern("W")
                .pattern("I")
                .unlockedBy("has_panel", has(PanelBlocks.CONTROL_PANEL))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelItems.CONTROL_LEVER_MODULE.get(), 2)
                .define('W', ItemTags.WOOL)
                .define('I', Tags.Items.INGOTS_IRON)
                .pattern(" W")
                .pattern("II")
                .unlockedBy("has_panel", has(PanelBlocks.CONTROL_PANEL))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelItems.INDICATOR_BULB_MODULE.get(), 2)
                .define('G', Tags.Items.GLASS_PANES)
                .define('I', Tags.Items.NUGGETS_IRON)
                .pattern("G")
                .pattern("I")
                .unlockedBy("has_panel", has(PanelBlocks.CONTROL_PANEL))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PanelItems.MOMENTARY_SWITCH_MODULE.get(), 2)
                .define('B', ItemTags.STONE_BUTTONS)
                .define('I', Tags.Items.INGOTS_IRON)
                .pattern("B")
                .pattern("I")
                .unlockedBy("has_panel", has(PanelBlocks.CONTROL_PANEL))
                .save(output);
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
                .define('C', Tags.Items.INGOTS_COPPER)
                .pattern("RRR")
                .pattern("ICI")
                .unlockedBy("has_copper", has(Tags.Items.INGOTS_COPPER));
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
