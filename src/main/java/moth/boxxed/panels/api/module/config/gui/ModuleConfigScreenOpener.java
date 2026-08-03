package moth.boxxed.panels.api.module.config.gui;

import moth.boxxed.panels.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

//Have to use another class just for opening because of a server side crash </3
public class ModuleConfigScreenOpener {
    public static void open(Module module, BlockPos pos) {
        Minecraft.getInstance().tell(() -> {
            Minecraft.getInstance().setScreen(new ModuleConfigScreen(module, pos));
        });
    }
}
