package moth.boxxed.panels.util;

import java.util.ArrayList;
import java.util.List;

public class PolyVoxel {
    private List<FlatAABB> boxes = new ArrayList<>();
    private FlatAABB bounds;

    public PolyVoxel() {
        this(null);
    }

    public PolyVoxel(int x1, int y1, int x2, int y2) {
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
            this.bounds = new FlatAABB(
                    Math.min(aabb.minX, this.bounds.minX),
                    Math.min(aabb.minY, this.bounds.minY),
                    Math.max(aabb.maxX, this.bounds.maxX),
                    Math.max(aabb.maxY, this.bounds.maxY)
            );
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

    @Override
    public String toString() {
        return "polyVoxel:{minX: %.4f, minY: %.4f, maxX: %.4f, maxY: %.4f}".formatted(this.bounds.minX, this.bounds.minY, this.bounds.maxX, this.bounds.maxY);
    }
}