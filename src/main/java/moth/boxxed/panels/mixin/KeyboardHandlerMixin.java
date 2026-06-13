package moth.boxxed.panels.mixin;

import moth.boxxed.panels.api.module.interaction.ModuleHoldInteractionManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0, opcode = 180),
            cancellable = true
    )
    public void panels$keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (this.minecraft.player != null && !this.minecraft.player.isSpectator()) {
            if (ModuleHoldInteractionManager.beforeKeyInput(key, scanCode, action, modifiers)) {
                ci.cancel();
            }
        }
    }
}
