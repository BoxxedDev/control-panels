package moth.boxxed.panels.content.modules.buzzer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.SableCompanion;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.io.IOutput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BuzzerModule extends Module implements IOutput {
    private int power = 0;

    public BuzzerModule(int x, int y) {
        super(PanelModules.BUZZER.get(), x, y);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("power", this.power);
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.power = tag.getInt("power");
        return super.loadData(tag, registries);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.BUZZER.render(poseStack, packedLight);

        if (this.power > 0) {
            Vec3 blockPos = this.getParentPos().getCenter();
            Vec3 playerPos = SableCompanion.INSTANCE.getEyePositionInterpolated(Minecraft.getInstance().player, partialTick);
            float angle = (float) (Math.atan2(blockPos.x-playerPos.x, blockPos.z-playerPos.z) + Math.PI/2f);

            poseStack.pushPose();
            float scale = Mth.map(this.power, 0, 15, 0.1f, 0.5f);
            poseStack.scale(scale, scale, scale);
            float xz = (1/scale)*0.125f;
            float y = (1/scale)*0.0625f;
            poseStack.translate(xz, y, xz);
            poseStack.mulPose(Axis.YP.rotation(angle));
            PanelPreloadedModels.BUZZ_INDICATOR.render(poseStack, packedLight);
            poseStack.popPose();
        }
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Block.box(0, 0, 0, 4, 1, 4);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 4, 4);
    }

    @Override
    public void setAnalog(int signal) {
        this.power = signal;
    }

    @Override
    public void onRemove(Player player) {
//        if (this.getLevel().isClientSide)
//            if (soundInstance != null) {
//                soundInstance.deactivate();
//                this.soundInstance = null;
//            }
    }

    @Override
    public void onUnloaded() {
//        if (this.getLevel().isClientSide)
//            if (soundInstance != null) {
//                soundInstance.deactivate();
//                this.soundInstance = null;
//            }
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide) {
            this.tickClient();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private BuzzSoundInstance soundInstance;

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        if (this.power == 0) {
            if (this.soundInstance != null) {
                this.soundInstance.deactivate();
                this.soundInstance = null;
            }
            return;
        }

        if (this.soundInstance == null || this.soundInstance.isStopped()) {
            Minecraft.getInstance().getSoundManager()
                    .play(this.soundInstance = new BuzzSoundInstance(this.getParentPos()));
        }
        this.soundInstance.resetLife();

        float p = Mth.map(this.power, 0, 15, 1, 2);
        this.soundInstance.setPitch(p);
    }
}
