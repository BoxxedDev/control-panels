package moth.boxxed.panels.api.panel.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.skin.ClientSkin;
import moth.boxxed.panels.api.panel.skin.PanelSkinsClientManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PanelSkinModel extends BakedModelWrapper<BakedModel> {
    public PanelSkinModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> base = super.getQuads(state, side, rand, extraData, renderType);

        if (!extraData.has(AbstractPanelBlockEntity.SKIN_PROPERTY))
            return base;

        ResourceLocation skinLocation = extraData.get(AbstractPanelBlockEntity.SKIN_PROPERTY);
        ClientSkin skin = PanelSkinsClientManager.MAP.get(skinLocation);
        if (skin == null)
            return base;

        AbstractPanelBlock.Shape shape = state.getValue(AbstractPanelBlock.SHAPE);
        Direction direction = state.getValue(AbstractPanelBlock.FACING);

        BakedModel skinModel = skin.getBlockModel(shape);
        Quaternionf rotation = Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 180 : 0));

        List<BakedQuad> ret = new ArrayList<>();
        for (BakedQuad quad : skinModel.getQuads(state, side, rand, extraData, renderType)) {
            ret.add(rotateQuad(quad, rotation));
        }

        return ret;
    }

    private static final int VERT_STRIDE = DefaultVertexFormat.BLOCK.getVertexSize()/4;

    private static BakedQuad rotateQuad(BakedQuad quad, Quaternionf rotation) {
        BakedQuad copy = new BakedQuad(Arrays.copyOf(quad.getVertices(), quad.getVertices().length),
                quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

        int[] verts = copy.getVertices();
        for (int i = 0; i < 4; i++) {
            float x = Float.intBitsToFloat(verts[i * VERT_STRIDE]);
            float y = Float.intBitsToFloat(verts[i * VERT_STRIDE + 1]);
            float z = Float.intBitsToFloat(verts[i * VERT_STRIDE + 2]);

            Vector3f vector = new Vector3f(x, y, z).sub(0.5f, 0.5f, 0.5f);
            rotation.transform(vector);
            vector.add(0.5f, 0.5f, 0.5f);

            verts[i * VERT_STRIDE] = Float.floatToRawIntBits(vector.x);
            verts[i * VERT_STRIDE + 1] = Float.floatToRawIntBits(vector.y);
            verts[i * VERT_STRIDE + 2] = Float.floatToRawIntBits(vector.z);
        }

        return copy;
    }
}
