package moth.boxxed.panels.compat.jei;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.Services;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.content.modules.key_switch.KeyItem;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.Arrays;
import java.util.List;

public class KeyColoringRecipeMaker {
    private static final String GROUP = "key.color";

    public static List<RecipeHolder<CraftingRecipe>> createRecipes() {
        Ingredient baseKey = Ingredient.of(PanelItems.KEY_ITEM);
        return Arrays.stream(DyeColor.values())
                .map(color -> createRecipe(color, baseKey))
                .toList();
    }

    private static RecipeHolder<CraftingRecipe> createRecipe(DyeColor color, Ingredient baseIngredient) {
        Ingredient colorIngredient = Ingredient.of(DyeItem.byColor(color));
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseIngredient, colorIngredient);
        KeyItem keyItem = KeyItem.fromColor(color);
        ItemStack output = new ItemStack(keyItem);
        ResourceLocation id = Dashpanels.path(GROUP + "." + output.getDescriptionId());
        CraftingRecipe recipe = new ShapelessRecipe(GROUP, CraftingBookCategory.MISC, output, inputs);
        return new RecipeHolder<>(id, recipe);
    }
}
