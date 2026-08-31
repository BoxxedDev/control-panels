package moth.boxxed.panels.content.panel.ceiling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.content.panel.wall.WallPanelBlock;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.util.FlatAABB;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.util.function.BiConsumer;

public class CeilingPanelBlockEntity extends AbstractPanelBlockEntity {
    public CeilingPanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelType.CEILING, PanelBlockEntities.CEILING_PANEL.get(), pos, blockState);
    }

    @Override
    public void transformPanelClipping(PoseStack stack) {
        Direction direction = this.getBlockState().getValue(WallPanelBlock.FACING);
        stack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        stack.pushPose();
        stack.translate(0, 0, 0.25f);
        stack.mulPose(Axis.XP.rotationDegrees(135));
        stack.translate(0, 0, -1.0625f);
    }

    @Override
    public boolean canPlaceModuleOnSurface(Vec3 position, Direction face) {
        if (face != Direction.DOWN && face != this.getBlockState().getValue(CeilingPanelBlock.FACING)) {
            return false;
        }

        return position.y < 0.75f && position.z > -0.25f;
    }

    @Override
    public Vector2i getPosForModule(Vec3 localSpace) {
        return new Vector2i(
                (int) Math.round(Mth.map(localSpace.x, -0.5, 0.5, 0, 16)),
                (int) Math.round(Mth.map(localSpace.z, 0.5, -0.25f, 0, 17))
        );
    }

    @Override
    public BiConsumer<Module, PoseStack> getIndividualModuleTransform() {
        return (module, stack) -> stack.translate(module.getPos().x/16f, 0, module.getPos().y/16f);
    }

    @Override
    public void renderTransform(PoseStack poseStack) {
        poseStack.translate(0, 0, 0.25f);
        poseStack.mulPose(Axis.XP.rotationDegrees(135));
        poseStack.translate(0, 0, -1.0625f);
    }

    @Override
    public Vector2i getContentArea() {
        return new Vector2i(16, 17);
    }
}
