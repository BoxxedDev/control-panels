package moth.boxxed.panels.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;

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

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType, int packedLight) {
        render(poseStack, bufferSource, renderType, packedLight, -1);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType, int packedLight, int colorPacked) {
        float red = ((colorPacked >> 16) & 0xFF)/255f;
        float green = ((colorPacked >> 8) & 0xFF)/255f;
        float blue = (colorPacked & 0xFF)/255f;
        
        // remap chunk bakery render types to entity rendering render types
        // if this is not done, all normals are ignored and lighting looks flat
        if (renderType == RenderType.SOLID) {
            renderType = Sheets.solidBlockSheet();
        } else if (renderType == RenderType.TRANSLUCENT) {
            renderType = Sheets.translucentCullBlockSheet();
        } else if (renderType == RenderType.CUTOUT) {
            renderType = Sheets.cutoutBlockSheet();
        } else if (renderType == RenderType.CUTOUT_MIPPED) {
            renderType = Sheets.cutoutBlockSheet();
        }

        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        RandomSource randomsource = RandomSource.create();
        long fixedSeed = 42L;

        for (Direction direction : Direction.values()) {
            randomsource.setSeed(fixedSeed);
            renderQuadList(poseStack.last(), consumer, red, green, blue, model.getQuads(null, direction, randomsource, ModelData.EMPTY, renderType), packedLight, OverlayTexture.NO_OVERLAY);
        }

        randomsource.setSeed(fixedSeed);
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
        float r = Mth.clamp(red, 0.0F, 1.0F);
        float g = Mth.clamp(green, 0.0F, 1.0F);
        float b = Mth.clamp(blue, 0.0F, 1.0F);
        for (BakedQuad bakedquad : quads) {
            consumer.putBulkData(pose, bakedquad, r, g, b, 1.0F, packedLight, packedOverlay);
        }
    }
}
