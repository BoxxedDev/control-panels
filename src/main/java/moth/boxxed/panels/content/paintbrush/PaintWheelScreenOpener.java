package moth.boxxed.panels.content.paintbrush;

import moth.boxxed.panels.api.panel.skin.ServerSkin;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class PaintWheelScreenOpener {
    public static void open(ServerSkin serverSkin, BlockPos pos) {
        Minecraft.getInstance().tell(() -> {
            Minecraft.getInstance().setScreen(new PaintWheelScreen(serverSkin, pos));
        });
    }
}
