package moth.boxxed.panels.content.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;

public class PanelRenderer implements BlockEntityRenderer<PanelBlockEntity> {
    @Override
    public void render(PanelBlockEntity panelBlockEntity, float partialTick, PoseStack poseStack,  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        boolean hit = Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult && hitResult.getBlockPos().equals(panelBlockEntity.getBlockPos());

        poseStack.pushPose();
        poseStack.translate(0, 0.75f, 0);
        Direction direction = panelBlockEntity.getBlockState().getValue(PanelBlock.FACING);
        poseStack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        for (Map.Entry<String, Module> entry : panelBlockEntity.getModules().entrySet()) {
            poseStack.pushPose();
            poseStack.translate(entry.getValue().getPos().x/16f+entry.getValue().getSize().x/16f, 0, entry.getValue().getPos().y/16f+entry.getValue().getSize().y/16f);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(0, 0, -0.25f);
            entry.getValue().render(panelBlockEntity, poseStack, partialTick, bufferSource, packedLight, packedOverlay);
            if (hit)
                entry.getValue().renderOutline(poseStack, bufferSource);
            poseStack.popPose();
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}