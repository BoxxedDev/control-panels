package moth.boxxed.panels.compat.jei;

import com.simibubi.create.compat.jei.GhostIngredientHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.compat.create.panel_link.screen.PanelLinkScreen;
import moth.boxxed.panels.content.modules.key_switch.KeyColoringRecipe;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.util.List;

@JeiPlugin
public class PanelJei implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return Dashpanels.path("jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        IModPlugin.super.registerGuiHandlers(registration);

        if (ModList.get().isLoaded("create"))
            registration.addGhostIngredientHandler(PanelLinkScreen.class, new GhostIngredientHandler());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(
                KeyCopyingRecipeMaker.createRecipe()
        ));
        registration.addRecipes(RecipeTypes.CRAFTING, KeyColoringRecipeMaker.createRecipes());
    }
}
