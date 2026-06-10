package moth.boxxed.panels.compat.sable.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.IHoverTooltip;
import moth.boxxed.panels.api.module.IMultiInput;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.compat.sable.PanelSableRegistries;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelPreloadedModels;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.*;

import java.lang.Math;
import java.util.List;
import java.util.function.BiConsumer;

public class NavballModule extends Module implements IMultiInput, IModuleLuaObject, IHoverTooltip {
    public NavballModule(int x, int y) {
        super(PanelSableRegistries.NAVBALL.get(), x, y, 6, 6);
    }

    private double getAngleYaw() {
        if (this.parentBlockEntity == null) return 0;
        Level level = this.parentBlockEntity.getLevel();
        if (level == null) return 0;

        Quaterniondc quaternion;
        if (level.isClientSide) {
            ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(this.parentBlockEntity);
            if (subLevel == null) return 0;
            quaternion = subLevel.renderPose().orientation();
        } else {
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(this.parentBlockEntity);
            if (subLevel == null) return 0;
            quaternion = subLevel.logicalPose().orientation();
        }

        return quaternion.getEulerAnglesXYZ(new Vector3d()).y * Mth.RAD_TO_DEG;
    }

    private double getAnglePitch() {
        if (this.parentBlockEntity == null) return 0;
        Level level = this.parentBlockEntity.getLevel();
        if (level == null) return 0;

        Quaterniondc quaternion;
        if (level.isClientSide) {
            ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(this.parentBlockEntity);
            if (subLevel == null) return 0;
            quaternion = subLevel.renderPose().orientation();
        } else {
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(this.parentBlockEntity);
            if (subLevel == null) return 0;
            quaternion = subLevel.logicalPose().orientation();
        }

        return quaternion.getEulerAnglesXYZ(new Vector3d()).z * Mth.RAD_TO_DEG;
    }

    private double getAngleRoll() {
        if (this.parentBlockEntity == null) return 0;
        Level level = this.parentBlockEntity.getLevel();
        if (level == null) return 0;

        Quaterniondc quaternion;
        if (level.isClientSide) {
            ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(this.parentBlockEntity);
            if (subLevel == null) return 0;
            quaternion = subLevel.renderPose().orientation();
        } else {
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(this.parentBlockEntity);
            if (subLevel == null) return 0;
            quaternion = subLevel.logicalPose().orientation();
        }

        return quaternion.getEulerAnglesXYZ(new Vector3d()).x * Mth.RAD_TO_DEG;
    }

    private int getAnalogYaw() {
        return (int) Mth.map(this.getAngleYaw(), 0, 360, 0, 15);
    }

    private int getAnalogPitch() {
        return (int) Mth.map(this.getAnglePitch(), 0, 360, 0, 15);
    }

    private int getAnalogRoll() {
        return (int) Mth.map(this.getAngleRoll(), 0, 360, 0, 15);
    }

    @Override
    public void getValues(BiConsumer<String, AnalogResult> consumer) {
        consumer.accept("yaw", this::getAnalogYaw);
        consumer.accept("pitch", this::getAnalogPitch);
        consumer.accept("roll", this::getAnalogRoll);
    }

    @Override
    public void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = panelBlockEntity.getLevel();
        BlockState state = panelBlockEntity.getBlockState();

        PanelPreloadedModels.NAVBALL_BASE.render(level, state, poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.pushPose();
        poseStack.translate( 0.1875f,0,  0.1875f);
        Quaternionf quat = new Quaternionf();
        ClientSubLevelAccess clientSubLevel = SableCompanion.INSTANCE.getContainingClient(panelBlockEntity);
        if (clientSubLevel != null)
            quat.set(clientSubLevel.renderPose(partialTick).orientation());
        poseStack.mulPose(quat);
//        Dashpanels.LOGGER.debug("\nRoll : {}\nPitch : {}\nYaw : {}", this.getAngleRoll(), this.getAnglePitch(), this.getAngleYaw());
        PanelPreloadedModels.NAVBALL.render(level, state, poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.popPose();
    }

    @Override
    public VoxelShape getShape() {
        return Shapes.or(
                Block.box(0.5, 0, -0.5, 5.5, 0.25, 6.5),
                Block.box(-0.5, 0, 0.5, 6.5, 0.25, 5.5)
        );
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getYaw", args -> this.getAngleYaw());
        consumer.accept("getPitch", args -> this.getAnglePitch());
        consumer.accept("getRoll", args -> this.getAngleRoll());
    }

    @Override
    public void addLines(List<Component> list) {
        list.add(Component.literal("Pitch : %.1f".formatted(this.getAnglePitch())).withStyle(ChatFormatting.BLUE));
        list.add(Component.literal("Yaw : %.1f".formatted(this.getAngleYaw())).withStyle(ChatFormatting.GREEN));
        list.add(Component.literal("Roll : %.1f".formatted(this.getAngleRoll())).withStyle(ChatFormatting.RED));
    }
}
