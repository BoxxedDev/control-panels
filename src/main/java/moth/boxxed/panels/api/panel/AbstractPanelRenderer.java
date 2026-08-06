package moth.boxxed.panels.api.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.SableCompanion;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleHitResult;
import moth.boxxed.panels.api.module.PlacementManager;
import moth.boxxed.panels.network.packet.SelectedModulePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import oshi.util.tuples.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class AbstractPanelRenderer<T extends AbstractPanelBlockEntity> implements BlockEntityRenderer<T> {
    protected void renderModules(T panelBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        panelBlockEntity.renderTransform(poseStack);
        LocalPlayer player = Minecraft.getInstance().player;
        boolean spectator = false;
        boolean guiHidden = Minecraft.getInstance().options.hideGui;
        if (player != null)
            spectator = player.isSpectator();
        boolean hit = Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult && hitResult.getBlockPos().equals(panelBlockEntity.getBlockPos());

        Module hitModule = null;
        Vec3 hitPosition = null;
        double hitDistance = Double.MAX_EXPONENT;
        if (hit) {
            for (Map.Entry<String, Module> entry : panelBlockEntity.getModules()) {
                Module module = entry.getValue();
                if (PlacementManager.isMovingModule(module))
                    continue;
                Vec3 eyePos = SableCompanion.INSTANCE.getEyePositionInterpolated(player, partialTick);
                Pair<Double, Vec3> result = Module.clipModule(
                        panelBlockEntity,
                        module,
                        new Vec3(module.getPos().x/16f, 0, module.getPos().y/16f),
                        eyePos,
                        player.getViewVector(partialTick),
                        partialTick
                );
                if (result != null && result.getA() < hitDistance) {
                    hitDistance = result.getA();
                    hitModule = module;
                    hitPosition = result.getB();
                }
            }
        }

        if (hitModule == null) {
            if (!panelBlockEntity.getSelectedModule(player).isEmpty()) {
                PacketDistributor.sendToServer(new SelectedModulePacket("", Optional.empty(), panelBlockEntity.getBlockPos()));
                panelBlockEntity.setSelectedModule(player, null, "");
            }
        } else if (!panelBlockEntity.getSelectedModule(player).equals(hitModule.getName()) || !hitPosition.equals(panelBlockEntity.getSelectedPosition(player))) {
            PacketDistributor.sendToServer(new SelectedModulePacket(hitModule.getName(), Optional.of(hitPosition), panelBlockEntity.getBlockPos()));
            panelBlockEntity.setSelectedModule(player, hitPosition, hitModule.getName());
        }

        BiConsumer<Module, PoseStack> individualModuleTransform = panelBlockEntity.getIndividualModuleTransform();
        for (Map.Entry<String, Module> entry : panelBlockEntity.getModules()) {
            Module module = entry.getValue();
            if (PlacementManager.isMovingModule(module))
                continue;

            poseStack.pushPose();
            individualModuleTransform.accept(module, poseStack);
            poseStack.pushPose();

            float aroundX = ((int) module.getSize().x)/32f;
            float aroundY = ((int) module.getSize().y)/32f;
            poseStack.rotateAround(Axis.YP.rotationDegrees(180), aroundX, 0, aroundY);
            module.render(panelBlockEntity, poseStack, partialTick, bufferSource, packedLight, packedOverlay);
            if (hit && !spectator && !guiHidden) {
                poseStack.rotateAround(Axis.YP.rotationDegrees(180), aroundX, 0, aroundY);
                ModuleHitResult hitResult = null;
                if (hitModule == module && hitPosition != null) {
                    hitResult = new ModuleHitResult(hitPosition.subtract(module.getPos().x/16f, 0, module.getPos().y/16f));
                }
                module.renderOutline(hitResult, poseStack, bufferSource, partialTick, hitModule == module ? 0xFFFFFF : 0x000000);
            }
            poseStack.popPose();
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}