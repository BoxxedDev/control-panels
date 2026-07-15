package moth.boxxed.panels.content.panel.wall;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public class WallPanelRenderer extends AbstractPanelRenderer<WallPanelBlockEntity> {
    @Override
    public void render(WallPanelBlockEntity panelBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction direction = panelBlockEntity.getBlockState().getValue(WallPanelBlock.FACING);

        poseStack.pushPose();
        poseStack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()== Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        poseStack.pushPose();
        poseStack.rotateAround(Axis.ZP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        poseStack.rotateAround(Axis.XP.rotationDegrees(90), 0.5f, 0.5f, 0.5f);
        poseStack.translate(0, 0.125f, 0.125f);
        this.renderModules(
                (module, stack) -> {
                    poseStack.translate(module.getPos().x/16f, 0, module.getPos().y/16f);
                },
                panelBlockEntity,
                partialTick,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
        poseStack.popPose();
    }
}