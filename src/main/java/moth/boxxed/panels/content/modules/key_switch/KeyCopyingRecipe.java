package moth.boxxed.panels.content.modules.key_switch;

import moth.boxxed.panels.index.PanelDataComponents;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.index.PanelRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class KeyCopyingRecipe extends CustomRecipe {
    public KeyCopyingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int keysWithNbt = 0;
        int writableKeys = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.is(PanelItems.KEY_ITEM)) {
                if (stack.has(PanelDataComponents.BOUND_MODULE)) {
                    keysWithNbt += stack.getCount();
                } else {
                    writableKeys += stack.getCount();
                }

                if (keysWithNbt > 1 || writableKeys > 15) {
                    return false;
                }
            }
        }

        return keysWithNbt == 1 && writableKeys >= 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack ret = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.is(PanelItems.KEY_ITEM)) {
                if (stack.has(PanelDataComponents.BOUND_MODULE)) {
                    ret = stack.copyWithCount(2);
                }
            }
        }
        return ret;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PanelRecipeSerializers.KEY_COPYING.get();
    }
}
