package moth.boxxed.panels.content.modules.buzzer;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.io.IOutput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.index.PanelSounds;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BuzzerModule extends Module implements IOutput {
    public BuzzerModule(int x, int y) {
        super(PanelModules.BUZZER.get(), x, y);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.BUZZER.render(poseStack, packedLight);
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
        Dashpanels.LOGGER.debug("Client: {}", this.getLevel().isClientSide);
        if (signal > 0)
            this.getLevel().playSound(null, this.getParentPos(), PanelSounds.BUZZ.get(), SoundSource.BLOCKS);
    }
}
