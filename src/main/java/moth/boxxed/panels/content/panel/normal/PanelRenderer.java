package moth.boxxed.panels.content.panel.normal;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.panel.AbstractPanelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

public class PanelRenderer extends AbstractPanelRenderer<PanelBlockEntity> {
    @Override
    public void render(PanelBlockEntity panelBlockEntity, float partialTick, PoseStack poseStack,  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(panelBlockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}