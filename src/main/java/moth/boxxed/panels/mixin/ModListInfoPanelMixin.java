package moth.boxxed.panels.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.wiki.WikiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ModListScreen.class)
public abstract class ModListInfoPanelMixin extends Screen {
    @Shadow
    private int listWidth;
    @Unique
    private Button panels$wikiButton;

    protected ModListInfoPanelMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void panels$addWikiButton(CallbackInfo ci, @Local(name = "y") int y) {
        this.addRenderableWidget(
                panels$wikiButton = Button.builder(
                        Component.translatable("dashpanels.wiki"),
                        (btn) -> Minecraft.getInstance().tell(() -> {
                            WikiScreen wikiScreen = new WikiScreen();
                            Minecraft.getInstance().setScreen(wikiScreen);
                        })
                ).bounds(listWidth+14, y+18, 48, 20).build()
        );
        panels$wikiButton.visible = false;
        panels$wikiButton.active = false;
    }

    @Inject(method = "updateCache", at = @At("TAIL"))
    private void panels$updateWikiButton(CallbackInfo ci, @Local(name = "selectedMod") IModInfo modInfo) {
        panels$wikiButton.visible = Objects.equals(modInfo.getModId(), Dashpanels.MOD_ID);
        panels$wikiButton.active = Objects.equals(modInfo.getModId(), Dashpanels.MOD_ID);
    }
}
