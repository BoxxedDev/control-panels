package moth.boxxed.panels.compat;

import moth.boxxed.panels.Dashpanels;
import net.neoforged.fml.ModList;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface PanelCompat {
    void init();
    String id();

    static void loadAll() {
        ServiceLoader<PanelCompat> loader = ServiceLoader.load(PanelCompat.class, PanelCompat.class.getClassLoader());
        Iterator<PanelCompat> iterator = loader.iterator();
        Dashpanels.LOGGER.debug("Compat List: %d".formatted(loader.stream().toList().size()));
        while (iterator.hasNext()) {
            PanelCompat compat = iterator.next();
            Dashpanels.LOGGER.debug(compat.id());
            if (ModList.get().isLoaded(compat.id())) {
                compat.init();
            }
        }
    }
}
