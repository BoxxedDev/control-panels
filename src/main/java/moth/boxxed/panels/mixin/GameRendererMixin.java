package moth.boxxed.panels.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "pick(F)V", at = @At("TAIL"))
    private void panels$pickPanelModules(float partialTicks, CallbackInfo ci) {
        if (this.minecraft == null) return;

        LocalPlayer player = this.minecraft.player;
        if (player == null) return;

        HitResult hitResult = this.minecraft.hitResult;

    }
}
