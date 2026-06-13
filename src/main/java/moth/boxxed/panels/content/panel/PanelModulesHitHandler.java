package moth.boxxed.panels.content.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import moth.boxxed.panels.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PanelModulesHitHandler {
    private static final Set<PanelBlockEntity> panels = new HashSet<>();

    public static void tick(PanelBlockEntity pbe) {
        if (invalid(pbe)) return;

        panels.add(pbe);
    }

    private static boolean invalid(PanelBlockEntity pbe) {
        if (pbe.isRemoved())
            return true;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null)
            return true;

        double reach = player.blockInteractionRange() + 2d;
        BlockPos pos = pbe.getBlockPos();
        return player.distanceToSqr(pos.getCenter()) > reach * reach;
    }

    public static void clearNear() {
        panels.removeIf(PanelModulesHitHandler::invalid);
    }

    public static Set<PanelBlockEntity> getNear() {
        return panels;
    }

    public static Double clipPanel(Vec3 eyePosMoj, Vec3 viewVectorMoj, PanelBlockEntity pbe, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return null;
        BlockPos pos = pbe.getBlockPos();

        Vector3d eyePos = JOMLConversion.toJOML(eyePosMoj);
        Vector3d viewVector = JOMLConversion.toJOML(viewVectorMoj);

        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(pbe);
        if (subLevel != null) {
            Pose3dc pose = subLevel.renderPose(partialTick);

            pose.transformPositionInverse(eyePos);
            pose.transformNormalInverse(viewVector);
        }

        PoseStack stack = new PoseStack();
        stack.pushPose();
        stack.translate(pos.getX()-eyePos.x, pos.getY()-eyePos.y, pos.getZ()-eyePos.z);

        Direction direction = pbe.getBlockState().getValue(PanelBlock.FACING);
        stack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        stack.translate(0, 0, 0.25f);

        Matrix4f pose = stack.last().pose();
        pose.invert();
        stack.popPose();

        Vector3f localViewPos = pose.transformPosition(new Vector3f());
        Vector3f localViewDir = pose.transformDirection(new Vector3f((float) viewVector.x, (float) viewVector.y, (float) viewVector.z));

        eyePos.set(localViewPos);
        viewVector.set(localViewDir).mul(player.blockInteractionRange()).add(eyePos);

        VoxelShape shape = Shapes.empty();
        for (Map.Entry<String, Module> moduleEntry : pbe.modules) {
            VoxelShape moduleShape = moduleEntry.getValue().getShape();
            shape = Shapes.or(shape, moduleShape.move(
                    moduleEntry.getValue().getPos().x/16f,
                    0.75f,
                    moduleEntry.getValue().getPos().y/16f
            ));
        }

        BlockHitResult hitResult = shape.clip(JOMLConversion.toMojang(eyePos), JOMLConversion.toMojang(viewVector), BlockPos.ZERO);

        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS)
            return null;

        Vec3 location = hitResult.getLocation();
        return eyePos.distanceSquared(location.x, location.y, location.z);
    }
}
