package moth.boxxed.panels.index;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PanelPaths {
    public static final Path GAME = FMLPaths.GAMEDIR.get();
    public static final Path PALETTES = GAME.resolve("skin_palettes");
}
