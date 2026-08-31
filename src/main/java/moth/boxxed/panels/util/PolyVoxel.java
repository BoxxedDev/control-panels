package moth.boxxed.panels.util;

import java.util.ArrayList;
import java.util.List;

public class PolyVoxel {
    private List<FlatAABB> boxes = new ArrayList<>();
    private FlatAABB bounds = null;

    public PolyVoxel() {
        this(null);
    }

    public PolyVoxel(double x1, double y1, double x2, double y2) {
        this(new FlatAABB(x1, y1, x2, y2));
    }

    public PolyVoxel(FlatAABB initialBox) {
        if (initialBox != null) {
            this.boxes.add(initialBox);
            this.bounds = initialBox;
        }
    }

    public PolyVoxel add(int x1, int y1, int x2, int y2) {
        return this.add(new FlatAABB(x1, y1, x2, y2));
    }

    public PolyVoxel add(FlatAABB aabb) {
        this.boxes.add(aabb);

        if (this.bounds != null) {
            this.bounds = this.bounds.minmax(aabb);
        } else {
            this.bounds = new FlatAABB(
                    aabb.minX,
                    aabb.minY,
                    aabb.maxX,
                    aabb.maxY
            );
        }
        return this;
    }

    public boolean collides(PolyVoxel polyVoxel) {
        if (!this.bounds.intersects(polyVoxel.bounds))
            return false;

        for (FlatAABB box : this.boxes) {
            for (FlatAABB otherBox : polyVoxel.boxes) {
                if (box.intersects(otherBox)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean contains(PolyVoxel other) {
        if (!this.getBounds().contains(other.getBounds())) {
            return false;
        }

        //TODO: actually figure out how to check if a shape contains another shape

        return true;
    }

    public FlatAABB getBounds() {
        return this.bounds;
    }

    public List<FlatAABB> getBoxes() {
        return this.boxes;
    }

    public PolyVoxel move(double x, double y) {
        PolyVoxel ret = new PolyVoxel();

        for (FlatAABB box : this.boxes) {
            ret.add(box.move(x, y));
        }

        return ret;
    }

    public PolyVoxel rotate(int degree) {
        if (Math.floorMod(degree, 90) != 0) {
            throw new IllegalArgumentException("Degree has to be an increment of 90");
        }

        double originalMinX = this.bounds.minX;
        double originalMinY = this.bounds.minY;

        PolyVoxel ret = new PolyVoxel();

        int correctedAngle = Math.floorMod(degree, 360);

        for (final FlatAABB box : this.getBoxes()) {
            ret.add(switch(correctedAngle) {
                case 0 -> new FlatAABB(box.minX, box.minY, box.maxX, box.maxY);
                case 90 -> new FlatAABB(box.minY, -box.minX, box.maxY, -box.maxX);
                case 180 -> new FlatAABB(-box.minX, -box.minY, -box.maxX, -box.maxY);
                case 270 -> new FlatAABB(-box.minY, box.minX, -box.maxY, box.maxX);
                default -> throw new IllegalStateException("Unexpected value: " + correctedAngle);
            });
        }

        return ret.move(
                -(ret.bounds.minX-originalMinX),
                -(ret.bounds.minY-originalMinY)
        );
    }

    @Override
    public String toString() {
        return "polyVoxel:{minX: %.4f, minY: %.4f, maxX: %.4f, maxY: %.4f}".formatted(this.bounds.minX, this.bounds.minY, this.bounds.maxX, this.bounds.maxY);
    }
}