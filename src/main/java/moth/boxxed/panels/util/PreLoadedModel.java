package moth.boxxed.panels.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moth.boxxed.panels.Dashpanels;
import net.createmod.ponder.render.VirtualRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelDataManager;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
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

    //TODO: fix flat looking lighting
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType, int packedLight) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        renderer.renderModel(
                poseStack.last(),
                bufferSource.getBuffer(renderType),
                null,
                this.model,
                1, 1, 1,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                ModelData.builder().build(),
                renderType
        );
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType, int packedLight, int colorPacked) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        float red = ((colorPacked >> 16) & 0xFF)/255f;
        float green = ((colorPacked >> 8) & 0xFF)/255f;
        float blue = (colorPacked & 0xFF)/255f;

        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        RandomSource randomsource = RandomSource.create();
        long i = 42L;

        for (Direction direction : Direction.values()) {
            randomsource.setSeed(42L);
            renderQuadList(poseStack.last(), consumer, red, green, blue, model.getQuads(null, direction, randomsource, ModelData.EMPTY, renderType), packedLight, OverlayTexture.NO_OVERLAY);
        }

        randomsource.setSeed(42L);
        renderQuadList(poseStack.last(), consumer, red, green, blue, model.getQuads(null, null, randomsource, ModelData.EMPTY, renderType), packedLight, OverlayTexture.NO_OVERLAY);
    }

    private static void renderQuadList(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float red,
            float green,
            float blue,
            List<BakedQuad> quads,
            int packedLight,
            int packedOverlay
    ) {
        float f = Mth.clamp(red, 0.0F, 1.0F);
        float f1 = Mth.clamp(green, 0.0F, 1.0F);
        float f2 = Mth.clamp(blue, 0.0F, 1.0F);
        for (BakedQuad bakedquad : quads) {
            consumer.putBulkData(pose, bakedquad, f, f1, f2, 1.0F, packedLight, packedOverlay);
        }
    }
}
