package moth.boxxed.panels.content.modules.key_switch;

import moth.boxxed.panels.index.PanelRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class KeyColoringRecipe extends CustomRecipe {
    public KeyColoringRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < input.size(); k++) {
            ItemStack itemstack = input.getItem(k);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof KeyItem) {
                    i++;
                } else {
                    if (!itemstack.is(net.neoforged.neoforge.common.Tags.Items.DYES)) {
                        return false;
                    }

                    j++;
                }

                if (j > 1 || i > 1) {
                    return false;
                }
            }
        }

        return i == 1 && j == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack itemstack = ItemStack.EMPTY;
        net.minecraft.world.item.DyeColor dyecolor = net.minecraft.world.item.DyeColor.WHITE;

        for (int i = 0; i < input.size(); i++) {
            ItemStack itemstack1 = input.getItem(i);
            if (!itemstack1.isEmpty()) {
                Item item = itemstack1.getItem();
                if (item instanceof KeyItem) {
                    itemstack = itemstack1;
                } else {
                    net.minecraft.world.item.DyeColor tmp = net.minecraft.world.item.DyeColor.getColor(itemstack1);
                    if (tmp != null) dyecolor = tmp;
                }
            }
        }

        KeyItem keyItem = KeyItem.fromColor(dyecolor);
        return itemstack.transmuteCopy(keyItem, 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PanelRecipeSerializers.KEY_COLORING.get();
    }
}
