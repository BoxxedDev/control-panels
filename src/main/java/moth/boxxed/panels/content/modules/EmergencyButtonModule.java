package moth.boxxed.panels.content.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

//I'll finish this once I patch allowing a module to have inputs and outputs
public class EmergencyButtonModule extends Module {
    public EmergencyButtonModule(@NonNull ModuleType<?> type, int x, int y) {
        super(type, x, y);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

    }

    @Override
    public VoxelShape getVoxelShape() {
        return null;
    }
}
