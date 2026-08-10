package moth.boxxed.panels.compat.jei;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.content.modules.key_switch.BoundModule;
import moth.boxxed.panels.index.PanelDataComponents;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.util.ShortUUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class KeyCopyingRecipeMaker {
    private static final String GROUP = "key.copy";
    private static final ResourceLocation LOCATION = Dashpanels.path(GROUP);

    public static RecipeHolder<CraftingRecipe> createRecipe() {
        ItemStack glintKey = new ItemStack(PanelItems.KEY_ITEM.get());
        glintKey.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        glintKey.set(PanelDataComponents.BOUND_MODULE, new BoundModule(
                BlockPos.ZERO,
                ShortUUID.fromInt(0xFFFFFFFF)
        ));

        NonNullList<Ingredient> inputs = NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(PanelItems.KEY_ITEM.get()),
                Ingredient.of(glintKey)
        );

        CraftingRecipe recipe = new ShapelessRecipe(GROUP, CraftingBookCategory.REDSTONE, glintKey.copyWithCount(2), inputs);
        return new RecipeHolder<>(LOCATION, recipe);
    }
}