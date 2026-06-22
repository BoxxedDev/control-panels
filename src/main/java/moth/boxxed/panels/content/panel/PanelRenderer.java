package moth.boxxed.panels.content.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.SableCompanion;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.panel.AbstractPanelRenderer;
import moth.boxxed.panels.network.packet.SelectedModulePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

public class PanelRenderer extends AbstractPanelRenderer<PanelBlockEntity> {
    @Override
    public void render(PanelBlockEntity panelBlockEntity, float partialTick, PoseStack poseStack,  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(panelBlockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}