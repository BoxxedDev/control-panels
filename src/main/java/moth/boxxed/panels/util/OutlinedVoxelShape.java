package moth.boxxed.panels.util;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import moth.boxxed.panels.mixin.VoxelShapeAccessor;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import oshi.util.tuples.Pair;

import java.util.List;

public class OutlinedVoxelShape extends SliceShape {
    private final VoxelShape collisionShape;
    private final List<Pair<Vec3, Vec3>> outlineEdges;

    public OutlinedVoxelShape(VoxelShape shape, List<Pair<Vec3, Vec3>> outlineEdges) {
        super(shape, Direction.Axis.X, 0);
        ((VoxelShapeAccessor) this).panels$setShape(((VoxelShapeAccessor) shape).panels$getShape());

        this.collisionShape = shape;
        this.outlineEdges = outlineEdges;
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        return this.collisionShape.getCoords(axis);
    }

    @Override
    public void forAllEdges(Shapes.DoubleLineConsumer action) {
        for (Pair<Vec3, Vec3> edge : this.outlineEdges) {
            final Vec3 pointA = edge.getA();
            final Vec3 pointB = edge.getB();
            action.consume(pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
        }
    }
}
