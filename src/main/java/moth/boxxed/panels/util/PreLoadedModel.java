package moth.boxxed.panels.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.HashMap;
import java.util.Map;

public class PreLoadedModel {
    public static final Map<ResourceLocation, PreLoadedModel> ALL_MODELS = new HashMap<>();

    private final ResourceLocation location;
    protected BakedModel model;

    private PreLoadedModel(ResourceLocation location) {
        this.location = location;
    }

    public static PreLoadedModel create(ResourceLocation modelLocation) {
        return ALL_MODELS.computeIfAbsent(modelLocation, PreLoadedModel::new);
    }

    public BakedModel get() {
        return this.model;
    }

    public void render(Level level, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType, int packedLight) {
        if (!level.isClientSide()) return;

        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        renderer.renderModel(
                poseStack.last(),
                bufferSource.getBuffer(renderType),
                state,
                this.model,
                1, 1, 1,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                renderType
        );
    }
}
