package moth.boxxed.panels.content.panel.modules.joystick;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.IMultiInput;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelHoldInteractions;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.BiConsumer;

public class JoystickModule extends Module implements IExternalUpdatable, IMultiInput, IModuleLuaObject {
    public float stickX = 0;
    public float stickY = 0;
    public boolean triggered = false;

    private float renderTriggered = 0;
    private float renderStickX = 0;
    private float renderStickY = 0;

    public JoystickModule(int x, int y) {
        super(PanelModules.JOYSTICK.get(), x, y, 4, 4);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putFloat("stick_x", stickX);
        tag.putFloat("stick_y", stickY);
        tag.putBoolean("triggered", triggered);

        tag.putFloat("render_x", renderStickX);
        tag.putFloat("render_y", renderStickY);
        tag.putFloat("render_triggered", renderTriggered);
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.stickX = tag.getFloat("stick_x");
        this.stickY = tag.getFloat("stick_y");
        this.triggered = tag.getBoolean("triggered");

        this.renderStickX = tag.getFloat("render_x");
        this.renderStickY = tag.getFloat("render_y");
        this.renderTriggered = tag.getFloat("render_triggered");
        return super.loadData(tag, registries);
    }

    @Override
    public InteractionResult onUse(Level level, Player player) {
        if (level.isClientSide && player.isLocalPlayer())
            if (!PanelHoldInteractions.JOYSTICK.isActive()) {
                PanelHoldInteractions.JOYSTICK.startHold(level, player, this);
                return InteractionResult.SUCCESS;
            }
        return super.onUse(level, player);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        this.renderStickX = org.joml.Math.lerp(this.renderStickX, this.stickX, 0.5f);
        this.renderStickY = org.joml.Math.lerp(this.renderStickY, this.stickY, 0.5f);
        this.renderTriggered = org.joml.Math.lerp(this.renderTriggered, this.triggered?0:1, 0.5f);
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float angleX = Mth.map(this.renderStickX, -1, 1, -7.5f, 7.5f);
        float angleY = Mth.map(this.renderStickY, -1, 1, -7.5f, 7.5f);
        float triggerAngle = Mth.map(this.renderTriggered, 0, 1, 5f, 22.5f);

        poseStack.pushPose();
        PanelPreloadedModels.JOYSTICK_BASE.render(poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.translate(0.125, 0.03125, 0.125);
        poseStack.mulPose(Axis.XP.rotationDegrees(angleY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(angleX));
        PanelPreloadedModels.JOYSTICK_BETWEEN.render(poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0, 0.03125, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(angleY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(angleX));
        PanelPreloadedModels.JOYSTICK_STICK.render(poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0, 0.125, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(triggerAngle));
        PanelPreloadedModels.JOYSTICK_TRIGGER.render(poseStack, bufferSource, RenderType.solid(), packedLight);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public VoxelShape getShape() {
        return Block.box(0, 0, 0, 4, 5, 4);
    }

    @Override
    public void getValues(BiConsumer<String, AnalogResult> consumer) {
        int analogRight = (int) Mth.map(Math.max(stickX, 0), 0, 1, 0, 15);
        int analogLeft = (int) Mth.map(-Math.min(stickX, 0), 0, 1, 0, 15);
        int analogTop = (int) Mth.map(Math.max(stickY, 0), 0, 1, 0, 15);
        int analogBottom = (int) Mth.map(-Math.min(stickY, 0), 0, 1, 0, 15);
        int analogTrigger = this.triggered ? 15 : 0;

        consumer.accept("right", () -> analogRight);
        consumer.accept("left", () -> analogLeft);
        consumer.accept("top", () -> analogTop);
        consumer.accept("bottom", () -> analogBottom);
        consumer.accept("trigger", () -> analogTrigger);
    }

    @Override
    public void setNum(List<Integer> num) {
        if (num.size() == 3) {
            this.stickX = num.getFirst()/100f;
            this.stickY = num.get(1)/100f;
            this.triggered = num.getLast()==1;
        }
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("getX", args -> this.stickX);
        consumer.accept("getY", args -> this.stickY);
    }
}
