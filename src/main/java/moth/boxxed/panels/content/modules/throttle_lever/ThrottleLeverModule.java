package moth.boxxed.panels.content.modules.throttle_lever;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.OutlinedVoxelShape;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

import static moth.boxxed.panels.index.PanelShapes.line;

public class ThrottleLeverModule extends Module {
    public ThrottleLeverModule(int x, int y) {
        super(PanelModules.THROTTLE_LEVER.get(), x, y);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.THROTTLE_LEVER_BASE.render(poseStack, packedLight);
    }

    @Override
    public VoxelShape getVoxelShape() {
        return new OutlinedVoxelShape(
                Block.box(0, 0, 0, 4, 2, 5),
                line(0, 0, 0, 4, 0, 0),
                line(0, 0, 5, 4, 0, 5),
                line(0, 0, 0, 0, 0, 5),
                line(4, 0, 0, 4, 0, 5),

                line(0, 2, 1, 4, 2, 1),
                line(0, 2, 4, 4, 2, 4),
                line(0, 2, 1, 0, 2, 4),
                line(4, 2, 1, 4, 2, 4),

                line(0, 0, 0, 0, 1, 0),
                line(4, 0, 0, 4, 1, 0),
                line(4, 0, 5, 4, 1, 5),
                line(0, 0, 5, 0, 1, 5),

                line(0, 1, 0, 0, 2, 1),
                line(4, 1, 0, 4, 2, 1),
                line(0, 1, 5, 0, 2, 4),
                line(4, 1, 5, 4, 2, 4),

                line(0, 1, 0, 4, 1, 0),
                line(0, 1, 5, 4, 1, 5)
        );
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 4, 5);
    }
}
