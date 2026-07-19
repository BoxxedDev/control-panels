package moth.boxxed.panels.util;

import net.minecraft.client.renderer.Rect2i;
import org.joml.Vector2i;

import java.awt.*;

public class RectUtil {
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
}
