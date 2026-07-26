package moth.boxxed.panels.util;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.mixin.PoseStackPoseAccessor;
import net.minecraft.util.Mth;
import org.joml.Vector2d;

public class MathUtil {
    public static Vector2d rotatePoint(double x, double y, int deg) {
        double rad = deg*Mth.DEG_TO_RAD;
        return new Vector2d(
                x*Math.cos(rad)-y*Math.sin(rad),
                x*Math.sin(rad)+y*Math.cos(rad)
        );
    }

    public static void scaleAround(PoseStack stack, float scaleX, float scaleY, float scaleZ, float x, float y, float z) {
        PoseStack.Pose pose = stack.last();
        pose.pose().scaleAround(scaleX, scaleY, scaleZ, x, y, z);
        if (Math.abs(scaleX) == Math.abs(scaleY) && Math.abs(scaleY) == Math.abs(scaleZ)) {
            if (scaleX < 0.0F || scaleY < 0.0F || scaleZ < 0.0F) {
                pose.normal().scale(Math.signum(scaleX), Math.signum(scaleY), Math.signum(scaleZ));
            }
        } else {
            pose.normal().scale(1.0F / scaleX, 1.0F / scaleY, 1.0F / scaleZ);
            ((PoseStackPoseAccessor)(Object) pose).bends$setTrustedNormals(false);
        }
    }
}
