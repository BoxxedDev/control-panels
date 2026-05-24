package moth.boxxed.panels.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "turnPlayer",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"),
            cancellable = true)
    private void panels$turnPlayer(double movementTime, CallbackInfo ci,
                                    @Local(ordinal = 4) double j, @Local(ordinal = 5) double k, @Local(ordinal = 0) int l) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isSpectator()) {
            if (ModuleHoldInteractionManager.onMouseMove(j, k*l)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPress",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0),
            cancellable = true)
    private void panels$onPress(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isSpectator()) {
            if (ModuleHoldInteractionManager.beforeMouseInput(button, action)) {
                ci.cancel();
            }
        }
    }
}
