package moth.boxxed.panels.content.paintbrush;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.config.ClientConfig;
import moth.boxxed.panels.index.PanelPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ColorPaletteStorage {
    public static final ColorPalette DEFAULT_PALETTE = new ColorPalette()
            .add(0xFFFFFF)
            .add(0x808080)
            .add(0x000000)
            .add(0xFF0000)
            .add(0xFF8000)
            .add(0xFFFF00)
            .add(0x80FF00)
            .add(0x00FF00)
            .add(0x00FF80)
            .add(0x00FFFF)
            .add(0x0080FF)
            .add(0x0000FF)
            .add(0xFF00FF)
            .add(0xFF0080);

    private static final String EXTENSION = ".skpal";

    public void createDefault() {
        Path defaultPath = PanelPaths.PALETTES.resolve("default" + EXTENSION);

        try {
            Files.createDirectory(PanelPaths.PALETTES);
        } catch (IOException ignored) {}

        try {
            Files.createFile(defaultPath);

            Files.write(defaultPath, DEFAULT_PALETTE.byteArray());
        } catch (FileAlreadyExistsException e) {
            Dashpanels.LOGGER.info("Default skin palette already exists");
        } catch (IOException e) {
            Dashpanels.LOGGER.error("An error occured while creating the default skin palette: {}", e.getMessage());
        }
    }

    public ColorPalette getDefaultPalette() {
        return this.allPalettesInFiles().getOrDefault(ClientConfig.DEFAULT_PALETTE.get(), DEFAULT_PALETTE);
    }

    public Map<String, ColorPalette> allPalettesInFiles() {
        Map<String, ColorPalette> ret = new HashMap<>();
        try (Stream<Path> stream = Files.walk(PanelPaths.PALETTES)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();

            for (Path filePath : files) {
                File file = filePath.toFile();
                String name = file.getName();
                int lastIndexOf = name.lastIndexOf('.');

                if (lastIndexOf == -1 || lastIndexOf == 0 || !name.substring(lastIndexOf).equals(EXTENSION))
                    continue;

                byte[] bytes = Files.readAllBytes(filePath);
                ColorPalette palette = ColorPalette.fromBytes(bytes);

                ret.put(name.substring(0, lastIndexOf), palette);
            }
        } catch (IOException e) {
            Dashpanels.LOGGER.error("Failed to walk through the palettes directory");
        }
        return ret;
    }

    public void storePalette(String name, ColorPalette palette) {
        Path newPalettePath = PanelPaths.PALETTES.resolve(name + EXTENSION);

        try {
            Files.createDirectory(PanelPaths.PALETTES);
        } catch (IOException ignored) {}

        int i=0;
        while (Files.exists(newPalettePath)) {
            newPalettePath = PanelPaths.PALETTES.resolve(name + "("+i+")" + EXTENSION);
            i++;
        }

        try {
            Files.createFile(newPalettePath);

            Files.write(newPalettePath, palette.byteArray());
        } catch (FileAlreadyExistsException e) {
            Dashpanels.LOGGER.info(name + " skin palette already exists");
        } catch (IOException e) {
            Dashpanels.LOGGER.error("An error occured while creating the {} skin palette: {}", name, e.getMessage());
        }
    }

    public boolean validateName(String str) {
        Set<String> files = this.allPalettesInFiles().keySet();
        return files.contains(str);
    }
}
