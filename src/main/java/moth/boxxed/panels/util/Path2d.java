package moth.boxxed.panels.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.Mth;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class Path2d {
    private final ImmutableList<Vector2d> points;
    private final float deltaDiff;

    public static Builder builder() {
        return new Builder();
    }

    protected Path2d(List<Vector2d> points) {
        this.points = ImmutableList.copyOf(points);
        this.deltaDiff = 1f/this.points.size();
    }

    public Vector2d getLerpedPoint(double delta) {
        List<Vector2d> current = new ArrayList<>(this.points);

        while (current.size() > 1) {
            List<Vector2d> next = new ArrayList<>(current.size()-1);

            for (int i = 0; i < current.size() - 1; i++) {
                Vector2d a = current.get(i);
                Vector2d b = current.get(i+1);

                double x = Mth.lerp(delta, a.x, b.x);
                double y = Mth.lerp(delta, a.y, b.y);

                next.add(new Vector2d(x, y));
            }

            current = next;
        }

        return current.getFirst();
    }

    public static class Builder {
        private final List<Vector2d> points = new ArrayList<>();

        public Builder add(double x, double y) {
            this.points.add(new Vector2d(x, y));
            return this;
        }

        public Path2d build() {
            if (this.points.isEmpty())
                throw new IllegalArgumentException("Point list cannot be empty.");
            return new Path2d(this.points);
        }
    }
}
