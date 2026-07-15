package moth.boxxed.panels.content.panel.wall;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlock;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class WallPanelBlockEntity extends AbstractPanelBlockEntity {
    public WallPanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelType.WALL, PanelBlockEntities.WALL_PANEL.get(), pos, blockState);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof WallPanelBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());
        Direction fromDirection = from.getValue(WallPanelBlock.FACING);

        if (fromDirection.getOpposite()==direction && to.getBlock() instanceof CableBlock) {
            return true;
        }
        return (fromDirection.getOpposite()!=direction &&
                fromDirection!=direction &&
                to.getBlock() instanceof AbstractPanelBlock &&
                to.getValue(AbstractPanelBlock.FACING) == fromDirection);
    }

    @Override
    public void transformPanelClipping(PoseStack stack) {
        Direction direction = this.getBlockState().getValue(WallPanelBlock.FACING);
        stack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        stack.pushPose();
        stack.rotateAround(Axis.ZP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        stack.rotateAround(Axis.XP.rotationDegrees(90), 0.5f, 0.5f, 0.5f);
        stack.translate(0, 0.125f, -0.125f);
        stack.translate(0, 0, 0.25f);
    }
}
