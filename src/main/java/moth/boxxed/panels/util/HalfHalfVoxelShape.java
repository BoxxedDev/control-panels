package moth.boxxed.panels.util;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import moth.boxxed.panels.mixin.VoxelShapeAccessor;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HalfHalfVoxelShape extends SliceShape {
    private final VoxelShape collisionShape;
    private final VoxelShape visualShape;

    public HalfHalfVoxelShape(VoxelShape shape, VoxelShape visual) {
        super(shape, Direction.Axis.X, 0);
        ((VoxelShapeAccessor) this).panels$setShape(((VoxelShapeAccessor) shape).panels$getShape());

        this.collisionShape = shape;
        this.visualShape = visual;
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        return this.collisionShape.getCoords(axis);
    }

    @Override
    public void forAllEdges(Shapes.DoubleLineConsumer action) {
        this.visualShape.forAllEdges(action);
    }
}
