package moth.boxxed.panels.content.modules.joystick;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.config.gui.ConfigFrameBuilder;
import moth.boxxed.panels.api.module.io.IMultiInput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.index.PanelHoldInteractions;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;

public class JoystickModule extends Module implements IExternalUpdatable, IMultiInput, IModuleLuaObject {
    public float stickX = 0;
    public float stickY = 0;
    public boolean triggered = false;

    private float lastRenderTriggered = 0;
    private float lastRenderStickX = 0;
    private float lastRenderStickY = 0;

    private float renderTriggered = 0;
    private float renderStickX = 0;
    private float renderStickY = 0;

    public final DeadzoneValue deadzoneValue = new DeadzoneValue(
            "deadzone",
            new Deadzone(0, 0, 0, 0)
    );
    public final ModuleConfigValue.IntValue output = new ModuleConfigValue.IntValue(
            "output",
            15,
            1, 15
    );
    public final ModuleConfigValue.BooleanValue inverted = new ModuleConfigValue.BooleanValue(
            "inverted",
            false
    );
    public final ModuleConfigValue.BooleanValue triggerInverted = new ModuleConfigValue.BooleanValue(
            "trigger_inverted",
            false
    );

    public JoystickModule(int x, int y) {
        super(PanelModules.JOYSTICK.get(), x, y);
    }

    @Override
    public void createConfig(ModuleConfig.Builder builder) {
        builder.add(deadzoneValue);
        builder.add(output);
        builder.add(inverted);
        builder.add(triggerInverted);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putFloat("stick_x", stickX);
        tag.putFloat("stick_y", stickY);
        tag.putBoolean("triggered", triggered);

        tag.putFloat("render_x", renderStickX);
        tag.putFloat("render_y", renderStickY);
        tag.putFloat("render_triggered", renderTriggered);

        tag.putFloat("last_render_x", lastRenderStickX);
        tag.putFloat("last_render_y", lastRenderStickY);
        tag.putFloat("last_render_triggered", lastRenderTriggered);
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

        this.lastRenderStickX = tag.getFloat("last_render_x");
        this.lastRenderStickY = tag.getFloat("last_render_y");
        this.lastRenderTriggered = tag.getFloat("last_render_triggered");
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
        this.lastRenderStickX = this.renderStickX;
        this.lastRenderStickY = this.renderStickY;
        this.lastRenderTriggered = this.renderTriggered;

        Deadzone deadzone = this.deadzoneValue.get();
        boolean insideRect = this.stickX < -deadzone.left || this.stickX > deadzone.right || this.stickY < -deadzone.bottom || this.stickY > deadzone.top;
        float targetX = insideRect ? this.stickX : 0;
        float targetY = insideRect ? this.stickY : 0;

        this.renderStickX = org.joml.Math.lerp(this.renderStickX, targetX, 0.5f);
        this.renderStickY = org.joml.Math.lerp(this.renderStickY, targetY, 0.5f);
        this.renderTriggered = org.joml.Math.lerp(this.renderTriggered, this.triggered?0:1, 0.5f);
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float angleX = Mth.map(Mth.lerp(partialTick, this.lastRenderStickX, this.renderStickX), -1, 1, -7.5f, 7.5f);
        float angleY = Mth.map(Mth.lerp(partialTick, this.lastRenderStickY, this.renderStickY), -1, 1, -7.5f, 7.5f);
        float triggerAngle = Mth.map(Mth.lerp(partialTick, this.lastRenderTriggered, this.renderTriggered), 0, 1, 5f, 22.5f);

        poseStack.pushPose();
        PanelPreloadedModels.JOYSTICK_BASE.render(poseStack, RenderType.solid(), packedLight);
        poseStack.translate(0.125, 0.03125, 0.125);
        poseStack.mulPose(Axis.XP.rotationDegrees(angleY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(angleX));
        PanelPreloadedModels.JOYSTICK_BETWEEN.render(poseStack, RenderType.solid(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0, 0.03125, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(angleY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(angleX));
        PanelPreloadedModels.JOYSTICK_STICK.render(poseStack, RenderType.solid(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0, 0.125, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(triggerAngle));
        PanelPreloadedModels.JOYSTICK_TRIGGER.render(poseStack, RenderType.solid(), packedLight);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Block.box(0, 0, 0, 4, 5, 4);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 4, 4);
    }

    @Override
    public void getValues(BiConsumer<String, AnalogResult> consumer) {
        Deadzone deadzone = this.deadzoneValue.get();
        boolean insideRect = this.stickX < -deadzone.left || this.stickX > deadzone.right || this.stickY < -deadzone.bottom || this.stickY > deadzone.top;
        float targetX = insideRect ? this.stickX : 0;
        float targetY = insideRect ? this.stickY : 0;

        int analogRight = (int) Mth.map(Math.max(targetX, 0), 0, 1, 0, this.output.get());
        int analogLeft = (int) Mth.map(-Math.min(targetX, 0), 0, 1, 0, this.output.get());
        int analogTop = (int) Mth.map(Math.max(targetY, 0), 0, 1, 0, this.output.get());
        int analogBottom = (int) Mth.map(-Math.min(targetY, 0), 0, 1, 0, this.output.get());
        int analogTrigger = this.triggered != this.triggerInverted.get() ? this.output.get() : 0;

        consumer.accept("right", () -> this.inverted.get() ? this.output.get() - analogRight : analogRight);
        consumer.accept("left", () -> this.inverted.get() ? this.output.get() - analogLeft : analogLeft);
        consumer.accept("top", () -> this.inverted.get() ? this.output.get() - analogTop : analogTop);
        consumer.accept("bottom", () -> this.inverted.get() ? this.output.get() - analogBottom : analogBottom);
        consumer.accept("trigger", () -> analogTrigger);
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {
        consumer.accept("stickX", args -> this.stickX);
        consumer.accept("stickY", args -> this.stickY);
        consumer.accept("triggered", args -> this.triggered);
        consumer.accept("deadzone", args -> new Float[]{
                deadzoneValue.get().left, deadzoneValue.get().right,
                    deadzoneValue.get().top, deadzoneValue.get().bottom
        });
    }

    @Override
    public void update(ServerPlayer player, CompoundTag tag, HolderLookup.Provider registries) {
        this.stickX = tag.getFloat("stick_x");
        this.stickY = tag.getFloat("stick_y");
        this.triggered = tag.getBoolean("triggered");
    }

    public static class DeadzoneValue extends ModuleConfigValue<Deadzone, DeadzoneValue> {
        public DeadzoneValue(String name, JoystickModule.@NonNull Deadzone defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putFloat("left", this.value.left);
            tag.putFloat("right", this.value.right);
            tag.putFloat("top", this.value.top);
            tag.putFloat("bottom", this.value.bottom);
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider registries) {
            float left = tag.getFloat("left");
            float right = tag.getFloat("right");
            float top = tag.getFloat("top");
            float bottom = tag.getFloat("bottom");
            this.value = new Deadzone(left, right, top, bottom);
        }

        @Override
        public void set(Deadzone value) {
            super.set(new Deadzone(
                    Math.clamp(value.left, 0, 1),
                    Math.clamp(value.right, 0, 1),
                    Math.clamp(value.top, 0, 1),
                    Math.clamp(value.bottom, 0, 1)
            ));
        }

        @Override
        public void buildGuiFrame(ConfigFrameBuilder builder) {
            builder.addEmpty();
            builder.addFloatBox(
                    this,
                    deadzone -> String.valueOf(deadzone.top),
                    (value, string) -> {
                        try {
                            Deadzone originalDeadzone = value.get();
                            float top = Float.parseFloat(string);
                            value.set(
                                    new Deadzone(originalDeadzone.left,
                                            originalDeadzone.right,
                                            top,
                                            originalDeadzone.bottom)
                            );
                        } catch (NumberFormatException e) {

                        }
                    }, 24
            );
            builder.nextRow();
            builder.addFloatBox(
                    this,
                    deadzone -> String.valueOf(deadzone.left),
                    (value, string) -> {
                        try {
                            Deadzone originalDeadzone = value.get();
                            float left = Float.parseFloat(string);
                            value.set(
                                    new Deadzone(left,
                                            originalDeadzone.right,
                                            originalDeadzone.top,
                                            originalDeadzone.bottom)
                            );
                        } catch (NumberFormatException e) {

                        }
                    }, 24
            );
            builder.addEmpty();
            builder.addFloatBox(
                    this,
                    deadzone -> String.valueOf(deadzone.right),
                    (value, string) -> {
                        try {
                            Deadzone originalDeadzone = value.get();
                            float right = Float.parseFloat(string);
                            value.set(
                                    new Deadzone(originalDeadzone.left,
                                            right,
                                            originalDeadzone.top,
                                            originalDeadzone.bottom)
                            );
                        } catch (NumberFormatException e) {

                        }
                    }, 24
            );
            builder.nextRow();
            builder.addEmpty();
            builder.addFloatBox(
                    this,
                    deadzone -> String.valueOf(deadzone.bottom),
                    (value, string) -> {
                        try {
                            Deadzone originalDeadzone = value.get();
                            float bottom = Float.parseFloat(string);
                            value.set(
                                    new Deadzone(originalDeadzone.left,
                                            originalDeadzone.right,
                                            originalDeadzone.top,
                                            bottom)
                            );
                        } catch (NumberFormatException e) {

                        }
                    }, 24
            );
        }
    }

    public record Deadzone(float left, float right, float top, float bottom) { }
}
