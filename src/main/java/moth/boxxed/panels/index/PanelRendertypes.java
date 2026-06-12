package moth.boxxed.panels.index;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class PanelRendertypes {
    private static final RenderStateShard.ShaderStateShard RENDERTYPE_TRANSLUCENT_GLOW_SHADER = new RenderStateShard.ShaderStateShard(PanelShaders::getTranslucentGlowShader);
    public static final RenderType TRANSLUCENT_GLOW = RenderType.create(
            "panels$translucent_glow",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            786432,
            true, true,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setShaderState(RENDERTYPE_TRANSLUCENT_GLOW_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                    .createCompositeState(true)
    );
}
