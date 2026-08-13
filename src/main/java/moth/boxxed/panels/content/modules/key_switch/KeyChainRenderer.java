package moth.boxxed.panels.content.modules.key_switch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.index.PanelDataComponents;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.CustomRendererItemModelWrapper;
import moth.boxxed.panels.util.Path2d;
import moth.boxxed.panels.util.PreLoadedModel;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import org.joml.Math;
import org.joml.Vector2d;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public class KeyChainRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Vector2d lerpP1 = new Vector2d(13, 7);
    private static final Vector2d lerpP2 = new Vector2d(13, 13);
    private static final Vector2d lerpP3 = new Vector2d(6, 13);

    public KeyChainRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        CustomRendererItemModelWrapper wrapper = (CustomRendererItemModelWrapper) Minecraft.getInstance().getItemRenderer()
                .getModel(stack, null, null, 0);

        if (!stack.has(PanelDataComponents.KEY_CHAIN_CONTENTS)) {
            return;
        }
        List<ItemStack> keyItems = Objects.requireNonNull(stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS)).items();

        if (displayContext != ItemDisplayContext.GUI) {
            PreLoadedModel.renderModel(
                    wrapper,
                    poseStack,
                    Sheets.translucentCullBlockSheet(),
                    packedLight,
                    packedOverlay
            );

            for (int i = 0; i < keyItems.size(); i++) {
                BakedModel model = Minecraft.getInstance().getItemRenderer()
                        .getModel(keyItems.get(i), null, null, 0);

                poseStack.pushPose();

                Vector2d lastPoint = bezier((double) i / keyItems.size() + 0.35f);

                poseStack.translate(lastPoint.x/16f - 0.5f, -lastPoint.y/16f + 0.3125f, 0);

                poseStack.rotateAround(Axis.ZP.rotationDegrees(90), 0.5f, 0.5f, 0.5f);
                poseStack.rotateAround(Axis.YP.rotationDegrees(45), 0.5f, 0.5f, 0.5f);
                poseStack.rotateAround(Axis.XP.rotationDegrees(90), 0.5f, 0.5f, 0.5f);

                PreLoadedModel.renderModel(
                        model,
                        poseStack,
                        Sheets.translucentCullBlockSheet(),
                        packedLight,
                        packedOverlay
                );
                poseStack.popPose();
            }
        } else {
            poseStack.pushPose();
            poseStack.translate(0, 0, -1);
            PreLoadedModel.renderModel(
                    wrapper,
                    poseStack,
                    Sheets.translucentCullBlockSheet(),
                    packedLight,
                    packedOverlay
            );
            poseStack.popPose();

            float xOffset = (keyItems.size()-1)/32f;
            float yOffset = (keyItems.size()-1)/32f;
            for (int i = 0; i < keyItems.size(); i++) {
                BakedModel model = Minecraft.getInstance().getItemRenderer()
                        .getModel(keyItems.get(i), null, null, 0);

                poseStack.pushPose();

                poseStack.translate((i/16f)-xOffset, yOffset-(i/16f), 0);

//                PanelPreloadedModels.KEY_OUTLINE.render(poseStack, packedLight);
                PreLoadedModel.renderModel(
                        model,
                        poseStack,
                        Sheets.translucentCullBlockSheet(),
                        packedLight,
                        packedOverlay
                );
                poseStack.popPose();
            }
        }
    }

    private static Vector2d bezier(double delta) {
        Vector2d p4 = new Vector2d(
                Mth.lerp(delta, lerpP1.x, lerpP2.x),
                Mth.lerp(delta, lerpP1.y, lerpP2.y)
        );
        Vector2d p5 = new Vector2d(
                Mth.lerp(delta, lerpP2.x, lerpP3.x),
                Mth.lerp(delta, lerpP2.y, lerpP3.y)
        );
        return new Vector2d(
                Mth.lerp(delta, p4.x, p5.x),
                Mth.lerp(delta, p4.y, p5.y)
        );
    }
}
