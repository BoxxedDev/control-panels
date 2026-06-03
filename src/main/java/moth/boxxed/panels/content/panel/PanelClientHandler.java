package moth.boxxed.panels.content.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PanelClientHandler {
    private static final PoseStack stack = new PoseStack();
    private static final Set<PanelBlockEntity> nearPanels = new HashSet<>();

    public static void tick(PanelBlockEntity be) {
        if (!invalid(be))
            nearPanels.add(be);
    }

    private static boolean invalid(PanelBlockEntity be) {
        if (be.isRemoved())
            return true;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null)
            return true;

        double reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue() + 2;
        BlockPos pos = be.getBlockPos();
        return player.distanceToSqr(pos.getCenter()) > reach*reach;
    }

    public static void clearNearPanels() {
        nearPanels.removeIf(PanelClientHandler::invalid);
    }

    public static Set<PanelBlockEntity> getNearPanels() {
        return nearPanels;
    }

    public static Double raycastModules(Vec3 eyePosOg, Vec3 viewVecOg, PanelBlockEntity pbe, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        BlockPos panelPos = pbe.getBlockPos();

        Vector3d eyePos = JOMLConversion.toJOML(eyePosOg);
        Vector3d viewVec = JOMLConversion.toJOML(viewVecOg);

        ClientSubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContainingClient(panelPos);
        if (subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.renderPose(partialTick);

            pose.transformPositionInverse(eyePos);
            pose.transformNormalInverse(viewVec);
        }

        Direction direction = pbe.getBlockState().getValue(PanelBlock.FACING);

        stack.pushPose();
        stack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        stack.translate(panelPos.getX()-eyePos.x, panelPos.getY()-eyePos.y, panelPos.getZ()-eyePos.z);
        Matrix4f pose = stack.last().pose();
        pose.invert();
        stack.popPose();

        stack.pushPose();
        stack.translate(panelPos.getX(), panelPos.getY(), panelPos.getZ());
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(stack, consumer, 0f, 0f, 0f, 1f, 1f, 1f, 1, 1, 1, 1);
        stack.popPose();

        Vector3f localViewPos = pose.transformPosition(new Vector3f());
        Vector3f localViewDir = pose.transformPosition(new Vector3f((float) viewVec.x, (float) viewVec.y, (float) viewVec.z));

        VoxelShape compoundShape = Block.box(0,0,0,1,1,1);
        for (Map.Entry<String, Module> entry : pbe.getModules()) {
            VoxelShape moduleShape = entry.getValue().getShape().move(entry.getValue().getPos().x/16d, 0.75d, entry.getValue().getPos().x/16d);
            if (compoundShape == Block.box(0,0,0,1,1,1)) {
                compoundShape = moduleShape;
            } else {
                compoundShape = Shapes.or(compoundShape, moduleShape);
            }
        }

        eyePos.set(localViewPos);
        viewVec.set(localViewDir).mul(player.blockInteractionRange()).add(eyePos);

        BlockHitResult hitResult = compoundShape.clip(JOMLConversion.toMojang(eyePos), JOMLConversion.toMojang(viewVec), BlockPos.ZERO);

        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS)
            return null;

        Vec3 location = hitResult.getLocation();
        return eyePos.distanceSquared(location.x, location.y, location.z);
    }
}
