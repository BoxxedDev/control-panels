package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.content.modules.key_switch.KeyColoringRecipe;
import moth.boxxed.panels.content.modules.key_switch.KeyCopyingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Dashpanels.MOD_ID);

    public static final Supplier<RecipeSerializer<KeyCopyingRecipe>> KEY_COPYING = RECIPE_SERIALIZERS.register(
            "crafting_special_keycopying",
            () -> new SimpleCraftingRecipeSerializer<>(KeyCopyingRecipe::new)
    );
    public static final Supplier<RecipeSerializer<KeyColoringRecipe>> KEY_COLORING = RECIPE_SERIALIZERS.register(
            "crafting_special_keycoloring",
            () -> new SimpleCraftingRecipeSerializer<>(KeyColoringRecipe::new)
    );

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
}
