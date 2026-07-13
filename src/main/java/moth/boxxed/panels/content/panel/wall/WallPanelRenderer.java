package moth.boxxed.panels.content.panel.wall;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.panel.AbstractPanelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

public class WallPanelRenderer extends AbstractPanelRenderer<WallPanelBlockEntity> {
    @Override
    public void render(WallPanelBlockEntity panelBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(panelBlockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}