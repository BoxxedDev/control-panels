package moth.boxxed.panels.util;

import com.mojang.math.Axis;
import moth.boxxed.panels.Dashpanels;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3d;

import java.awt.*;
import java.util.List;

public class ShapeUtil {
    public static Vector2i clampRectPosToArea(Rect2i area, Rect2i inner) {
        int bottomRightAreaX = area.getX()+area.getWidth();
        int bottomRightAreaY = area.getY()+area.getHeight();
        int bottomRightInnerX = inner.getX()+inner.getWidth();
        int bottomRightInnerY = inner.getY()+inner.getHeight();

        int retX = inner.getX();
        int retY = inner.getY();

        if (bottomRightInnerX > bottomRightAreaX) {
            retX = bottomRightAreaX-inner.getWidth();
        }
        if (bottomRightInnerY > bottomRightAreaY) {
            retY = bottomRightAreaY-inner.getHeight();
        }
        if (inner.getX() < area.getX()) {
            retX = area.getX();
        }
        if (inner.getY() < area.getY()) {
            retY = area.getX();
        }

        return new Vector2i(retX, retY);
    }

    public static boolean intersects(Rect2i a, Rect2i b) {
        Rectangle javaRectA = new Rectangle(a.getX(), a.getY(), a.getWidth(), a.getHeight());
        Rectangle javaRectB = new Rectangle(b.getX(), b.getY(), b.getWidth(), b.getHeight());

        return javaRectA.intersects(javaRectB);
    }

    public static Vector2d clampAABBPosToAABB(FlatAABB area, FlatAABB inner) {
        double retX = inner.minX;
        double retY = inner.minY;

        if (inner.maxX > area.maxX) {
            retX = area.maxX-inner.sizeX();
        }
        if (inner.maxY > area.maxY) {
            retY = area.maxY-inner.sizeY();
        }
        if (inner.minX < area.minX) {
            retX = area.minX;
        }
        if (inner.minY < area.minY) {
            retY = area.minY;
        }

        return  new Vector2d(retX, retY);
    }

    /**
     * Voxel shape version of {@link PolyVoxel#rotate(int)}
     * @param axis
     * @param original
     * @param degree
     * @return
     */
    public static VoxelShape rotateVoxelShape(Direction.Axis axis, VoxelShape original, int degree) {
        if (Math.floorMod(degree, 90) != 0) {
            throw new IllegalArgumentException("Degree has to be an increment of 90");
        }

        final int correctedDegree = Math.floorMod(degree, 360);
        final AABB originalBounds = original.bounds();
        final List<AABB> originalBoxes = original.toAabbs();

        //Idk how much this optimizes it but just in case yk?
        if (correctedDegree == 0) {
            return original;
        }

        double originalMinX = originalBounds.minX;
        double originalMinY = originalBounds.minY;
        double originalMinZ = originalBounds.minZ;

        //GO MY BILLION SWITCH STATEMENT
        //PS: I have no idea if the x or z work, I kinda just did a similar thing for the y axis so it may not work, if you really want/need to just make a PR
        return switch (axis) {
            case X -> {
                VoxelShape shape = Shapes.empty();
                for (AABB box : originalBoxes) {
                    final double negMinY = Math.min(-box.minY, -box.maxY);
                    final double negMaxY = Math.max(-box.minY, -box.maxY);
                    final double negMinZ = Math.min(-box.minZ, -box.maxZ);
                    final double negMaxZ = Math.max(-box.minZ, -box.maxZ);
                    shape = switch (correctedDegree) {
                        case 90 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        box.minX, Math.min(box.minZ, box.maxZ), negMinY,
                                        box.maxX, Math.max(box.minZ, box.maxZ), negMaxY
                                ), BooleanOp.OR
                        );
                        case 180 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        box.minX, negMinY, negMinZ,
                                        box.maxX, negMaxY, negMaxZ
                                ), BooleanOp.OR
                        );
                        case 270 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        box.minX, negMinZ, Math.min(box.minY, box.maxY),
                                        box.maxX, negMaxZ, Math.max(box.minY, box.maxY)
                                ), BooleanOp.OR
                        );
                        default -> throw new IllegalStateException("Unexpected value: " + Math.floorMod(degree, 360));
                    };
                }
                AABB newBounds = shape.bounds();
                yield shape.move(
                        0,
                        -(newBounds.minY - originalMinY),
                        -(newBounds.minZ - originalMinZ)
                );
            }
            case Y -> {
                VoxelShape shape = Shapes.empty();
                for (AABB box : originalBoxes) {
                    final double negMinX = Math.min(-box.minX, -box.maxX);
                    final double negMaxX = Math.max(-box.minX, -box.maxX);
                    final double negMinZ = Math.min(-box.minZ, -box.maxZ);
                    final double negMaxZ = Math.max(-box.minZ, -box.maxZ);
                    shape = switch (correctedDegree) {
                        case 90 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        Math.min(box.minZ, box.maxZ), box.minY, negMinX,
                                        Math.max(box.minZ, box.maxZ), box.maxY, negMaxX
                                ), BooleanOp.OR
                        );
                        case 180 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        negMinX, box.minY, negMinZ,
                                        negMaxX, box.maxY, negMaxZ
                                        ), BooleanOp.OR
                        );
                        case 270 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        negMinZ, box.minY, Math.min(box.minX, box.maxX),
                                        negMaxZ, box.maxY, Math.max(box.minX, box.maxX)
                                        ), BooleanOp.OR
                        );
                        default -> throw new IllegalStateException("Unexpected value: " + Math.floorMod(degree, 360));
                    };
                }
                AABB newBounds = shape.bounds();
                yield shape.move(
                    -(newBounds.minX - originalMinX),
                    0,
                        -(newBounds.minZ - originalMinZ)
                );
            }
            case Z -> {
                VoxelShape shape = Shapes.empty();
                for (AABB box : originalBoxes) {
                    final double negMinX = Math.min(-box.minX, -box.maxX);
                    final double negMaxX = Math.max(-box.minX, -box.maxX);
                    final double negMinY = Math.min(-box.minY, -box.maxY);
                    final double negMaxY = Math.max(-box.minY, -box.maxY);
                    shape = switch (correctedDegree) {
                        case 90 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        Math.min(box.minY, box.maxY), negMinX, box.minZ,
                                        Math.max(box.minY, box.maxY), negMaxX, box.maxZ
                                ), BooleanOp.OR
                        );
                        case 180 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        negMinX, negMinY, box.minZ,
                                        negMaxX, negMaxY, box.maxZ
                                ), BooleanOp.OR
                        );
                        case 270 -> Shapes.joinUnoptimized(
                                shape, Shapes.create(
                                        negMinY, Math.min(box.minX, box.maxX), box.minZ,
                                        negMaxY, Math.max(box.minX, box.maxX), box.maxZ
                                ), BooleanOp.OR
                        );
                        default -> throw new IllegalStateException("Unexpected value: " + Math.floorMod(degree, 360));
                    };
                }
                AABB newBounds = shape.bounds();
                yield shape.move(
                        -(newBounds.minX - originalMinX),
                        -(newBounds.minY - originalMinY),
                        0
                );
            }
        };
    }

    public static AABB rotateAABBAround(AABB aabb, Vector3d center, Quaternionf orientation) {
        Vector3d min = new Vector3d(aabb.minX, aabb.minY, aabb.minZ);
        Vector3d max = new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ);

        min.sub(center);
        max.sub(center);

        orientation.transform(min);
        orientation.transform(max);

        min.add(center);
        max.add(center);

        return new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
    }
}
