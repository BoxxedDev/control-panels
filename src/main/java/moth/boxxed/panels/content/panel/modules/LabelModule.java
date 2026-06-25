package moth.boxxed.panels.content.panel.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LabelModule extends Module {
    public LabelModule(int x, int y) {
        super(PanelModules.LABEL.get(), x, y, 4, 2);
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0, 0.001f, 0);
        PanelPreloadedModels.LABEL.render(poseStack, bufferSource, RenderType.cutout(), packedLight);
        poseStack.popPose();
    }

    @Override
    public VoxelShape getShape() {
        return Shapes.or(
                Block.box(0, 0, 0, 5, 0.001, 1),
                Block.box(-1, 0, 1, 4, 0.001, 2)
        );
    }
}
