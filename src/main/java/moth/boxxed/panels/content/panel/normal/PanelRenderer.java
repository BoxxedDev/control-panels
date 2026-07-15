package moth.boxxed.panels.content.panel.normal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public class PanelRenderer extends AbstractPanelRenderer<PanelBlockEntity> {
    @Override
    public void render(PanelBlockEntity panelBlockEntity, float partialTick, PoseStack poseStack,  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction direction = panelBlockEntity.getBlockState().getValue(AbstractPanelBlock.FACING);

        poseStack.pushPose();
        poseStack.translate(0, 0.75f, 0);
        poseStack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()== Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        this.renderModules(
                (module, stack) -> {
                    float offsetX = 0;
                    if (module.getPos().x == 0) {
                        offsetX = 0.0001f;
                    } else if (module.getPos().x+module.getSize().x == 16) {
                        offsetX = -0.0001f;
                    }
                    poseStack.translate(module.getPos().x/16f+module.getSize().x/16f+offsetX, 0, module.getPos().y/16f+module.getSize().y/16f);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    poseStack.translate(0, 0, -0.25f);
                },
                panelBlockEntity,
                partialTick,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }
}