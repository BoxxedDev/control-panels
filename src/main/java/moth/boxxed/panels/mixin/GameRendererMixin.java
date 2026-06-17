package moth.boxxed.panels.mixin;

import dev.ryanhcode.sable.companion.SableCompanion;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.content.panel.PanelModulesHitHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    private void panels$pickWholePanel(float partialTicks, CallbackInfo ci) {
        if (this.minecraft == null) return;
        LocalPlayer player = this.minecraft.player;
        if (player == null) return;
        Vec3 eyePos = SableCompanion.INSTANCE.getEyePositionInterpolated(player, partialTicks);
        HitResult normalHit = this.minecraft.hitResult;
        double minDist = normalHit != null && normalHit.getType() != HitResult.Type.MISS ? SableCompanion.INSTANCE.distanceSquaredWithSubLevels(player.level(), eyePos, normalHit.getLocation()) : Double.MAX_VALUE;
        for (AbstractPanelBlockEntity pbe : PanelModulesHitHandler.getNear()) {
            if (pbe.isRemoved()) continue;

            Double hitResultDist = PanelModulesHitHandler.clipPanel(eyePos, player.getViewVector(partialTicks), pbe, partialTicks);
            if (hitResultDist != null) {
                if (hitResultDist < minDist) {
                    minDist = hitResultDist;
                    this.minecraft.hitResult = new BlockHitResult(pbe.getBlockPos().getCenter(), Direction.UP, pbe.getBlockPos(), false);
                }
            }
        }
    }
}
