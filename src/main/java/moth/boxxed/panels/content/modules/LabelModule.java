package moth.boxxed.panels.content.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LabelModule extends Module {
    public LabelModule(int x, int y) {
        super(PanelModules.LABEL.get(), x, y);
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0, 0.001f, 0);
        PanelPreloadedModels.LABEL.render(poseStack, RenderType.cutout(), packedLight);
        poseStack.popPose();
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Shapes.or(
                Block.box(1, 0, 0, 6, 0.001, 1),
                Block.box(0, 0, 1, 5, 0.001, 2)
        );
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(1, 0, 6, 1)
                .add(0, 1, 5, 2);
    }
}
