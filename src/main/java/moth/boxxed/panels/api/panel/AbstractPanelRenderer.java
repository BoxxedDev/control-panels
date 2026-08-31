package moth.boxxed.panels.api.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.SableCompanion;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleHitResult;
import moth.boxxed.panels.api.module.placement.PlacementManager;
import moth.boxxed.panels.network.packet.SelectedModulePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
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
            final Vec2 maxHitPos = new Vec2((float) (hitModule.getSize().x/16f), (float) (hitModule.getSize().y/16f));
            final Vec3 nonCorrected = hitPosition.subtract(hitModule.getPos().x/16f, 0, hitModule.getPos().y/16f);
            final Vec3 corrected = switch (hitModule.getRotation()) {
                case ZERO -> nonCorrected;
                case NINETY -> new Vec3(maxHitPos.y-nonCorrected.z, nonCorrected.y, maxHitPos.x-nonCorrected.x);
                case ONE_EIGHTY -> new Vec3(maxHitPos.x-nonCorrected.x, nonCorrected.y, maxHitPos.y-nonCorrected.z);
                case TWO_SEVENTY -> new Vec3(nonCorrected.z, nonCorrected.y, nonCorrected.x);
            };

            PacketDistributor.sendToServer(new SelectedModulePacket(hitModule.getName(), Optional.of(corrected), panelBlockEntity.getBlockPos()));
            panelBlockEntity.setSelectedModule(player, corrected, hitModule.getName());
        }

        BiConsumer<Module, PoseStack> individualModuleTransform = panelBlockEntity.getIndividualModuleTransform();
        for (Map.Entry<String, Module> entry : panelBlockEntity.getModules()) {
            Module module = entry.getValue();
            if (PlacementManager.isMovingModule(module))
                continue;

            poseStack.pushPose();
            individualModuleTransform.accept(module, poseStack);
            poseStack.pushPose();

//            poseStack.rotateAround(Axis.YP.rotationDegrees(180 + module.getRotation().getAngle()), (float) (center.x/16f), 0, (float) (center.y/16f));

            poseStack.pushPose();
//            poseStack.mulPose(Axis.YP.rotationDegrees(180 + module.getRotation().getAngle()));

            double sizeX = module.getShape().rotate(module.getRotation().getAngle()).getBounds().sizeX();
            double sizeY = module.getShape().rotate(module.getRotation().getAngle()).getBounds().sizeY();

            float xTranslation = (float) switch (module.getRotation()) {
                case ZERO, NINETY -> sizeX;
                case ONE_EIGHTY, TWO_SEVENTY -> 0.0F;
            };
            float yTranslation = (float) switch (module.getRotation()) {
                case ZERO, TWO_SEVENTY -> sizeY;
                case NINETY, ONE_EIGHTY -> 0.0F;
            };

            poseStack.translate(xTranslation/16f, 0, yTranslation/16f);
            poseStack.mulPose(Axis.YP.rotationDegrees(180 + module.getRotation().getAngle()));
            module.render(panelBlockEntity, poseStack, partialTick, bufferSource, packedLight, packedOverlay);

            poseStack.popPose();

            if (hit && !spectator && !guiHidden) {
                poseStack.pushPose();

                float lineBoxXTranslation = (float) switch (module.getRotation()) {
                    case ZERO, NINETY -> 0;
                    case ONE_EIGHTY, TWO_SEVENTY -> sizeX;
                };
                float lineBoxYTranslation = (float) switch (module.getRotation()) {
                    case ZERO, TWO_SEVENTY -> 0;
                    case NINETY, ONE_EIGHTY -> sizeY;
                };

                poseStack.translate(lineBoxXTranslation/16f, 0, lineBoxYTranslation/16f);
                poseStack.mulPose(Axis.YP.rotationDegrees(module.getRotation().getAngle()));

                ModuleHitResult hitResult = null;
                if (hitModule == module) {
                    hitResult = new ModuleHitResult(
                            panelBlockEntity.getSelectedPosition(player)
                    );
                }

                module.renderOutline(hitResult, poseStack, bufferSource, partialTick, hitModule == module ? 0xFFFFFF : 0x000000);

                poseStack.popPose();
            }

            poseStack.popPose();
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}