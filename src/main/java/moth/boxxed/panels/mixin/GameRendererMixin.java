package moth.boxxed.panels.mixin;

import dev.ryanhcode.sable.companion.SableCompanion;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.content.panel.PanelClientHandler;
import net.minecraft.client.DeltaTracker;
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
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "pick(F)V", at = @At("TAIL"))
    private void panels$pickPanelModules(float partialTicks, CallbackInfo ci) {
        if (this.minecraft == null) return;

        LocalPlayer player = this.minecraft.player;
        if (player == null) return;

        Vec3 eyePos = SableCompanion.INSTANCE.getEyePositionInterpolated(player, partialTicks);
        HitResult hitResult = this.minecraft.hitResult;
        double minDist = hitResult != null && hitResult.getType() != HitResult.Type.MISS ?
                SableCompanion.INSTANCE.distanceSquaredWithSubLevels(player.level(), eyePos, hitResult.getLocation()) : Double.MAX_VALUE;

        for (PanelBlockEntity pbe : PanelClientHandler.getNearPanels()) {
            if (pbe.isRemoved()) continue;

            Double hitResultDistance = PanelClientHandler.raycastModules(eyePos, player.getViewVector(partialTicks), pbe, partialTicks);
            if (hitResultDistance == null) continue;
            if (hitResultDistance >= minDist) continue;
            minDist = hitResultDistance;
            this.minecraft.hitResult = new BlockHitResult(pbe.getBlockPos().getCenter(), Direction.UP, pbe.getBlockPos(), false);
        }
    }
}