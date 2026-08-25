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

    public PolyVoxel add(double x1, double y1, double x2, double y2) {
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

    public FlatAABB getBounds() {
        return this.bounds;
    }

    public List<FlatAABB> getBoxes() {
        return this.boxes;
    }

    public PolyVoxel move(int x, int y) {
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

        double centerX = this.bounds.maxX-this.bounds.minX;
        double centerY = this.bounds.maxY-this.bounds.minY;

        PolyVoxel ret = new PolyVoxel();

        for (FlatAABB box : this.getBoxes()) {
            ret.add(
                    box.move(-centerX, -centerY)
                            .rotate(degree)
                            .move(centerX, centerY)
            );
        }

        return ret;
    }

    @Override
    public String toString() {
        return "polyVoxel:{minX: %.4f, minY: %.4f, maxX: %.4f, maxY: %.4f}".formatted(this.bounds.minX, this.bounds.minY, this.bounds.maxX, this.bounds.maxY);
    }
}