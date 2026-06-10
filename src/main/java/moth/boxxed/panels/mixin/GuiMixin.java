package moth.boxxed.panels.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteractionManager;
import moth.boxxed.panels.index.PanelHoldInteractions;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderCrosshair",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 0),
            cancellable = true)
    private void panels$renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isSpectator()) {
            for (ModuleHoldInteraction<?> interaction : ModuleHoldInteractionManager.INTERACTIONS) {
                if (interaction.isActive())
                    if (!interaction.getGuiContext().crosshairVisibility) {
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.disableBlend();
                        ci.cancel();
                    }
            }
        }
    }
}
