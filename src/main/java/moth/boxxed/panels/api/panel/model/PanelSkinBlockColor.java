package moth.boxxed.panels.api.panel.model;

import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.skin.ClientSkin;
import moth.boxxed.panels.api.panel.skin.PanelSkinsClientManager;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.awt.*;

public class PanelSkinBlockColor implements BlockColor {
    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (level == null || pos == null)
            return 0xFFFFFFFF;

        if (level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe) {
            ClientSkin skin = PanelSkinsClientManager.MAP.get(pbe.skin);
            if (skin != null && skin.tintable().orElse(false)) {
                float[] hsb = Color.RGBtoHSB((pbe.skinColor >> 16) & 0xFF, (pbe.skinColor >> 8) & 0xFF, pbe.skinColor & 0xFF, null);

                float h = hsb[0];
                float sat = Mth.clampedMap(hsb[1], 0, 1, 0, 0.5f);
                float br = Mth.clampedMap(hsb[2], 0, 1, 0.25f, 1);

                return Color.HSBtoRGB(h, sat, br);
            }
        }

        return 0xFFFFFFFF;
    }
}
