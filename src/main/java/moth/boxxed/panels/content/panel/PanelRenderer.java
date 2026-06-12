package moth.boxxed.panels.content.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.SableCompanion;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.network.packet.SelectedModulePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

public class PanelRenderer implements BlockEntityRenderer<PanelBlockEntity> {
    @Override
    public void render(PanelBlockEntity panelBlockEntity, float partialTick, PoseStack poseStack,  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        LocalPlayer player = Minecraft.getInstance().player;
        boolean spectator = false;
        boolean guiHidden = Minecraft.getInstance().options.hideGui;
        if (player != null)
            spectator = player.isSpectator();
        boolean hit = Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult && hitResult.getBlockPos().equals(panelBlockEntity.getBlockPos());

        poseStack.pushPose();
        poseStack.translate(0, 0.75f, 0);
        Direction direction = panelBlockEntity.getBlockState().getValue(PanelBlock.FACING);
        poseStack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);

        Module hitModule = null;
        double hitDistance = Double.MAX_EXPONENT;
        if (hit) {
            for (Map.Entry<String, Module> entry : panelBlockEntity.getModules()) {
                Module module = entry.getValue();
                Vec3 eyePos = SableCompanion.INSTANCE.getEyePositionInterpolated(player, partialTick);
                Double result = Module.clipModule(
                        panelBlockEntity,
                        module,
                        new Vec3(module.getPos().x/16f, 0.75, module.getPos().y/16f),
                        eyePos,
                        player.getViewVector(partialTick),
                        partialTick
                );
                if (result != null && result < hitDistance) {
                    hitDistance = result;
                    hitModule = module;
                }
            }
        }

        if (hitModule == null) {
            if (!panelBlockEntity.getSelectedModules(player).isEmpty()) {
                PacketDistributor.sendToServer(new SelectedModulePacket("", panelBlockEntity.getBlockPos()));
                panelBlockEntity.setSelectedModule(player, "");
            }
        } else if (!panelBlockEntity.getSelectedModules(player).equals(hitModule.getName())) {
            PacketDistributor.sendToServer(new SelectedModulePacket(hitModule.getName(), panelBlockEntity.getBlockPos()));
            panelBlockEntity.setSelectedModule(player, hitModule.getName());
        }

        for (Map.Entry<String, Module> entry : panelBlockEntity.getModules()) {
            poseStack.pushPose();
            Module module = entry.getValue();
            float offsetX = 0;
            if (module.getPos().x == 0) {
                offsetX = 0.0001f;
            } else if (module.getPos().x+module.getSize().x == 16) {
                offsetX = -0.0001f;
            }
            poseStack.translate(module.getPos().x/16f+module.getSize().x/16f+offsetX, 0, module.getPos().y/16f+module.getSize().y/16f);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(0, 0, -0.25f);
            module.render(panelBlockEntity, poseStack, partialTick, bufferSource, packedLight, packedOverlay);
            if (hit && !spectator && !guiHidden)
                module.renderOutline(poseStack, bufferSource, partialTick, hitModule == module ? 0xFFFFFF : 0x000000);
            poseStack.popPose();
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}