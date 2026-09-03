package moth.boxxed.panels.content.modules.throttle_lever;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.module.io.IInput;
import moth.boxxed.panels.api.module.io.IMultiInput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.ModuleMethodBuilder;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.OutlinedVoxelShape;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;

import static moth.boxxed.panels.index.PanelShapes.line;

public class ThrottleLeverModule extends Module implements IMultiInput, IExternalUpdatable {
    public ThrottleLeverModule(int x, int y) {
        super(PanelModules.THROTTLE_LEVER.get(), x, y);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.THROTTLE_LEVER_BASE.render(poseStack, packedLight);

        poseStack.pushPose();

        PanelPreloadedModels.THROTTLE_LEVER_HANDLE.render(poseStack, packedLight);

        poseStack.popPose();


        poseStack.pushPose();

        poseStack.rotateAround(Axis.YP.rotationDegrees(180), 2/16f, 0, 2.5f/16f);
        PanelPreloadedModels.THROTTLE_LEVER_HANDLE.render(poseStack, packedLight);

        poseStack.popPose();

        poseStack.pushPose();
        PanelPreloadedModels.THROTTLE_LEVER_CENTER_BUTTON.render(poseStack, packedLight);
        poseStack.popPose();
    }

    //Worried about the constant object creation
    private static final VoxelShape SHAPE = new OutlinedVoxelShape(
            Block.box(0, 0, 0, 4, 2, 5),
            line(0, 0, 0, 4, 0, 0),
            line(0, 0, 5, 4, 0, 5),
            line(0, 0, 0, 0, 0, 5),
            line(4, 0, 0, 4, 0, 5),

            line(0, 2, 1, 4, 2, 1),
            line(0, 2, 4, 4, 2, 4),
            line(0, 2, 1, 0, 2, 4),
            line(4, 2, 1, 4, 2, 4),

            line(0, 0, 0, 0, 1, 0),
            line(4, 0, 0, 4, 1, 0),
            line(4, 0, 5, 4, 1, 5),
            line(0, 0, 5, 0, 1, 5),

            line(0, 1, 0, 0, 2, 1),
            line(4, 1, 0, 4, 2, 1),
            line(0, 1, 5, 0, 2, 4),
            line(4, 1, 5, 4, 2, 4),

            line(0, 1, 0, 4, 1, 0),
            line(0, 1, 5, 4, 1, 5)
    );

    @Override
    public VoxelShape getVoxelShape() {
        return SHAPE;
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 4, 5);
    }

    @Override
    public void buildComputerMethods(ModuleMethodBuilder builder) {
        super.buildComputerMethods(builder);
    }

    @Override
    public void update(ServerPlayer player, CompoundTag tag, HolderLookup.Provider registries) {

    }

    @Override
    public void getValues(BiConsumer<String, AnalogResult> consumer) {

    }
}
