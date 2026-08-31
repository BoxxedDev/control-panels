package moth.boxxed.panels.util;

import org.joml.Vector2d;

public class FlatAABB {
    public final double minX;
    public final double minY;
    public final double maxX;
    public final double maxY;

    public FlatAABB(double x1, double y1, double x2, double y2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
    }

    public boolean intersects(FlatAABB aabb) {
        return this.intersects(aabb.minX, aabb.minY, aabb.maxX, aabb.maxY);
    }

    //For some reason the original AABB class has a "intersects" method which should really be named "contains" since it sees if an AABB is contained rather than if the two boxes are actually intersecting, it pmo.
    public boolean intersects(double x1, double y1, double x2, double y2) {
        return this.minX < x2 && this.maxX > x1 && this.minY < y2 && this.maxY > y1;
    }

    public boolean contains(double x, double y) {
        return this.minX <= x && this.maxX >= x && this.minY <= y && this.maxY >= y;
    }

    public boolean contains(FlatAABB other) {
        return this.minX <= other.minX &&
                this.minY <= other.minY &&
                this.maxX >= other.maxX &&
                this.maxY >= other.maxY;
    }

    public boolean containsExclusive(double x, double y) {
        return this.minX < x && this.maxX > x && this.minY < y && this.maxY > y;
    }

    public FlatAABB move(double x, double y) {
        return new FlatAABB(this.minX+x, this.minY+y, this.maxX+x, this.maxY+y);
    }

    public FlatAABB rotate(int degree) {
        if (Math.floorMod(degree, 90) != 0) {
            throw new IllegalArgumentException("Degree has to be an increment of 90");
        }

        int correctedAngle = Math.floorMod(degree, 360);

        FlatAABB ret = switch(correctedAngle) {
            case 0 -> new FlatAABB(this.minX, this.minY, this.maxX, this.maxY);
            case 90 -> new FlatAABB(this.minY, -this.minX, this.maxY, -this.maxX);
            case 180 -> new FlatAABB(-this.minX, -this.minY, -this.maxX, -this.maxY);
            case 270 -> new FlatAABB(-this.minY, this.minX, -this.maxY, this.maxX);
            default -> throw new IllegalStateException("Unexpected value: " + correctedAngle);
        };

        return ret.move(-(ret.minX - this.minX), -(ret.minY - this.minY));
    }

    public double sizeX() {
        return this.maxX-this.minX;
    }

    public double sizeY() {
        return this.maxY-this.minY;
    }

    public Vector2d center() {
        return new Vector2d(
                (this.minX + this.maxX)/2,
                (this.minY + this.maxY)/2
        );
    }

    public FlatAABB minmax(FlatAABB other) {
        return new FlatAABB(
                Math.min(this.minX, other.minX),
                Math.min(this.minY, other.minY),
                Math.max(this.maxX, other.maxX),
                Math.max(this.maxY, other.maxY)
        );
    }

    @Override
    public String toString() {
        return "FlatAABB: { " +
                "minX : " + this.minX +
                ", minY : " + this.minY +
                ", maxX : " + this.maxX +
                ", maxY : " + this.maxY +
                " }";
    }
}
