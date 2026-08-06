package moth.boxxed.panels.util;

public class Rect2d {
    public double minX;
    public double minY;
    public double maxX;
    public double maxY;

    public double width;
    public double height;

    public Rect2d(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        this.width = maxX - minX;
        this.height = maxY - minY;
    }

    public boolean contains(double x, double y) {
        return x>=this.minX && x<=this.maxX && y>=this.minY && y<=this.maxY;
    }

    public boolean contains(Rect2d other) {
        return other.minX>=this.minX && other.maxX<=this.maxX && other.minY>=this.minY && other.maxY<=this.maxY;
    }

    public boolean intersects(Rect2d other) {
        return !(this.maxX <= other.minX || this.minX >= other.maxX || this.maxY <= other.minY || this.minY >= other.maxY);
    }

    public net.minecraft.client.renderer.Rect2i toRect2i() {
        return new net.minecraft.client.renderer.Rect2i((int) this.minX, (int) this.minY, (int) this.width, (int) this.height);
    }

    public static Rect2d fromRect2i(net.minecraft.client.renderer.Rect2i rect2i) {
        return new Rect2d(rect2i.getX(), rect2i.getY(), rect2i.getX()+ rect2i.getWidth(), rect2i.getY()+ rect2i.getHeight());
    }
}
