package moth.boxxed.panels.index;

import com.mojang.math.Axis;
import moth.boxxed.panels.util.OutlinedVoxelShape;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PanelShapes {
    public static final Map<Direction, VoxelShape> PANEL_SHAPE = forHorizontal(
        aabb(16,12,16),
            aabb(0, 12, 0, 16, 16, 4)
    );
    public static final Map<Direction, VoxelShape> WALL_PANEL_SHAPE = forHorizontal(
            aabb(16,16,2)
    );
    public static final Map<Direction, VoxelShape> CEILING_PANEL_SHAPE = forHorizontal(
            Stream.concat(
                    Arrays.stream(new AABB[]{aabb(16, 16, 6), aabb(0, 10, 6, 16, 16, 16)}),
                    IntStream.range(1, 5).mapToObj(i -> aabb(
                            0, (4-i)*2+2, (6-i)*2+4,
                            16, 10, ((4-i)+1)*2+4
                    ))).toArray(AABB[]::new)//,
//            line(0, 0, 0, 16, 0, 0),
//            line(0, 0, 0, 0, 16, 0),
//            line(0, 16, 0, 16, 16, 0),
//            line(16, 0, 0, 16, 16, 0),
//            line(0, 16, 0, 0, 16, 16),
//            line(16, 16, 0, 16, 16, 16),
//            line(0, 16, 16, 16, 16, 16),
//            line(0, 0, 0, 0, 0, 4),
//            line(16, 0, 0, 16, 0, 4),
//            line(0, 0, 4, 16, 0, 4),
//            line(0, 12, 16, 0, 16, 16),
//            line(16, 12, 16, 16, 16, 16),
//            line(0, 12, 16, 16, 12, 16),
//            line(0, 0, 4, 0, 12, 16),
//            line(16, 0, 4, 16, 12, 16)
    );

    private static AABB aabb(double sizeX, double sizeY, double sizeZ) {
        return new AABB(0, 0, 0, sizeX/16, sizeY/16, sizeZ/16);
    }

    private static AABB aabb(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AABB(minX/16, minY/16, minZ/16, maxX/16, maxY/16, maxZ/16);
    }

    private static Pair<Vec3, Vec3> line(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new Pair<>(
                new Vec3(x1/16, y1/16, z1/16),
                new Vec3(x2/16, y2/16, z2/16)
        );
    }

    private static AABB rotateAABB(AABB aabb, Quaternionf orientation) {
        Vector3d min = new Vector3d(aabb.minX, aabb.minY, aabb.minZ);
        Vector3d max = new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ);

        min.sub(0.5, 0.5, 0.5);
        max.sub(0.5, 0.5, 0.5);

        orientation.transform(min);
        orientation.transform(max);

        min.add(0.5, 0.5, 0.5);
        max.add(0.5, 0.5, 0.5);

        return new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public static Map<Direction, VoxelShape> forHorizontal(AABB... boxesArray) {
        return forHorizontal(direction -> direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180), boxesArray);
    }

    public static Map<Direction, VoxelShape> forHorizontal(Function<Direction, Float> directionRotationFactory,
                                                           AABB... boxesArray) {
        Map<Direction, VoxelShape> ret = new HashMap<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            float rotation = directionRotationFactory.apply(direction);
            Quaternionf orientation = Axis.YP.rotationDegrees(rotation);
            VoxelShape shape = Shapes.empty();
            for (AABB box : boxesArray) {
                AABB rotatedBox = rotateAABB(box, orientation);
                shape = Shapes.joinUnoptimized(shape, Shapes.box(
                        Math.min(rotatedBox.minX, rotatedBox.maxX),
                        Math.min(rotatedBox.minY, rotatedBox.maxY),
                        Math.min(rotatedBox.minZ, rotatedBox.maxZ),
                        Math.max(rotatedBox.minX, rotatedBox.maxX),
                        Math.max(rotatedBox.minY, rotatedBox.maxY),
                        Math.max(rotatedBox.minZ, rotatedBox.maxZ)
                        ), BooleanOp.OR
                );
            }
            ret.put(direction, shape);
        }
        return ret;
    }

    @SafeVarargs
    public static Map<Direction, OutlinedVoxelShape> forHorizontalOutlined(AABB[] boxesArray, Pair<Vec3, Vec3>... edges) {
        return forHorizontalOutlined(direction -> direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180), boxesArray, edges);
    }

    @SafeVarargs
    public static Map<Direction, OutlinedVoxelShape> forHorizontalOutlined(Function<Direction, Float> directionRotationFactory,
                                                                           AABB[] boxesArray, Pair<Vec3, Vec3>... edges) {
        Map<Direction, OutlinedVoxelShape> ret = new HashMap<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            float rotation = directionRotationFactory.apply(direction);
            Quaternionf orientation = Axis.YP.rotationDegrees(rotation);
            VoxelShape shape = Shapes.empty();
            List<Pair<Vec3, Vec3>> rotatedEdges = new ArrayList<>();
            for (AABB box : boxesArray) {
                AABB rotatedBox = rotateAABB(box, orientation);
                shape = Shapes.joinUnoptimized(shape, Shapes.box(
                                Math.min(rotatedBox.minX, rotatedBox.maxX),
                                Math.min(rotatedBox.minY, rotatedBox.maxY),
                                Math.min(rotatedBox.minZ, rotatedBox.maxZ),
                                Math.max(rotatedBox.minX, rotatedBox.maxX),
                                Math.max(rotatedBox.minY, rotatedBox.maxY),
                                Math.max(rotatedBox.minZ, rotatedBox.maxZ)
                        ), BooleanOp.OR
                );
            }
            for (Pair<Vec3, Vec3> edge : edges) {
                Vector3d pointA = new Vector3d(edge.getA().toVector3f());
                Vector3d pointB = new Vector3d(edge.getB().toVector3f());

                pointA.sub(0.5, 0.5, 0.5);
                pointB.sub(0.5, 0.5, 0.5);

                orientation.transform(pointA);
                orientation.transform(pointB);

                pointA.add(0.5, 0.5, 0.5);
                pointB.add(0.5, 0.5, 0.5);

                rotatedEdges.add(new Pair<>(
                        new Vec3(pointA.x, pointA.y, pointA.z),
                        new Vec3(pointB.x, pointB.y, pointB.z)
                ));
            }
            ret.put(direction, new OutlinedVoxelShape(shape, rotatedEdges));
        }
        return ret;
    }
}
