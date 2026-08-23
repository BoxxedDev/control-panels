package moth.boxxed.panels.content.modules.knob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleHitResult;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.io.IInput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.compat.computercraft.ModuleLuaException;
import moth.boxxed.panels.compat.computercraft.ModuleMethodBuilder;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;

import java.util.function.BiConsumer;

//TODO: Maybe make it so you can use this while hovering it with a scroll wheel
public class KnobModule extends Module implements IExternalUpdatable, IInput {
    private float lastRenderAngle = 0;
    private float renderAngle = 0;
    private int angle = 0;

    public final ModuleConfigValue.IntRangeValue angleRange = new ModuleConfigValue.IntRangeValue("angle",
            0, 360, 0, 360).addChangeListener(
            (oldV, newV) -> {
                this.angle = Math.clamp(newV.getMinimum(), newV.getMaximum(), this.angle);
            }
    );
    public final ModuleConfigValue.IntRangeValue outputRange = new ModuleConfigValue.IntRangeValue("output",
            0, 15, 0, 15);

    public KnobModule(int x, int y) {
        super(PanelModules.KNOB.get(), x, y);
    }

    @Override
    public void createConfig(ModuleConfig.Builder builder) {
        builder.add(angleRange);
        builder.add(outputRange);
    }

    @Override
    public InteractionResult onUse(Level level, Player player) {
        if (level.isClientSide && player.isLocalPlayer())
            if (!PanelHoldInteractions.KNOB.isActive()) {
                PanelHoldInteractions.KNOB.startHold(level, player, this);
                return InteractionResult.SUCCESS;
            }
        return super.onUse(level, player);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        this.lastRenderAngle = this.renderAngle;
        this.renderAngle = Math.lerp(this.renderAngle, this.angle, 0.75f);
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Block.box(0, 0, 0, 2, 2, 2);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 2, 2);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("num", this.angle);
        tag.putFloat("render_angle", this.renderAngle);
        tag.putFloat("last_render_angle", this.lastRenderAngle);

        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.angle = tag.getInt("num");
        this.renderAngle = tag.getFloat("render_angle");
        this.lastRenderAngle = tag.getFloat("last_render_angle");

        return super.loadData(tag, registries);
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.rotateAround(Axis.YP.rotationDegrees(Mth.lerp(partialTick, this.lastRenderAngle, this.renderAngle)-45), 1/16f, 0, 1/16f);
        PanelPreloadedModels.KNOB.render(poseStack, RenderType.solid(), packedLight);
        poseStack.popPose();
    }

    @Override
    public void renderOutline(ModuleHitResult hitResult, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int rgb) {
        poseStack.pushPose();
        poseStack.rotateAround(Axis.YP.rotationDegrees(Mth.lerp(partialTick, this.lastRenderAngle, this.renderAngle)-45), 1/16f, 0, 1/16f);
        super.renderOutline(hitResult, poseStack, bufferSource, partialTick, rgb);
        poseStack.popPose();
    }

    public int getAngle() {
        return this.angle;
    }

    @Override
    public int getAnalog() {
        return Math.round(Mth.map(this.angle, 0, 360, this.outputRange.get().getMinimum(), this.outputRange.get().getMaximum()));
    }

    @Override
    public void update(ServerPlayer player, CompoundTag tag, HolderLookup.Provider registries) {
        this.angle = tag.getInt("angle");
        float f = (angle+360)/360f;
        this.parentBlockEntity.getLevel().playSound(null, this.getParentPos(), SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.1f, f);
    }

    @Override
    public void buildComputerMethods(ModuleMethodBuilder builder) {
        builder.addReturn("getAngle", args -> this.getAngle());
        builder.addReturn("getSignal", args -> this.getAnalog());
        builder.addVoid("setAngle", args -> {
            if (args.count() != 1)
                throw new ModuleLuaException("Arg amount cannot be less than or greater than 1");
            if (args.get(0) instanceof Number number) {
                this.angle = Math.clamp(number.intValue(), this.angleRange.get().getMinimum(), this.angleRange.get().getMaximum());
            } else {
                throw new ModuleLuaException("First arg has to be an integer");
            }
        });
    }
}
