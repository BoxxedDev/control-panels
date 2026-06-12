package moth.boxxed.panels.mixin;

import net.mcexpanded.fancytabsections.FancyTabSections;
import net.mcexpanded.fancytabsections.creativetab.BannerRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Fix fancy tabs not rendering over slot
@Mixin(net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreen {
    @Shadow
    private static CreativeModeTab selectedTab;

    @Inject(method = "render", at = @At("TAIL"))
    private void panels$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ResourceLocation tab = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(selectedTab);
        if (FancyTabSections.SECTIONS_MAP.containsKey(tab))
        {
            BannerRenderer.render((net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen) (Object) this, guiGraphics, FancyTabSections.SECTIONS_MAP.get(tab));
        }
    }
}
