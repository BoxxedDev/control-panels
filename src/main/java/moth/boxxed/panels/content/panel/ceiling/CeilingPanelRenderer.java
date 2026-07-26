package moth.boxxed.panels.content.panel.ceiling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public class CeilingPanelRenderer extends AbstractPanelRenderer<CeilingPanelBlockEntity> {
    @Override
    public void render(CeilingPanelBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction direction = blockEntity.getBlockState().getValue(AbstractPanelBlock.FACING);

        poseStack.pushPose();
        poseStack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()== Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        this.renderModules(
                blockEntity,
                partialTick,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }
}
