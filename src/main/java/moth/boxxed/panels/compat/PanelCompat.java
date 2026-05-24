package moth.boxxed.panels.compat;

import moth.boxxed.panels.ControlPanels;
import net.neoforged.fml.ModList;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface PanelCompat {
    void init();
    String id();

    static void loadAll() {
        ServiceLoader<PanelCompat> loader = ServiceLoader.load(PanelCompat.class, PanelCompat.class.getClassLoader());
        Iterator<PanelCompat> iterator = loader.iterator();
        ControlPanels.LOGGER.debug("Compat List: %d".formatted(loader.stream().toList().size()));
        while (iterator.hasNext()) {
            PanelCompat compat = iterator.next();
            ControlPanels.LOGGER.debug(compat.id());
            if (ModList.get().isLoaded(compat.id())) {
                compat.init();
            }
        }
    }
}
