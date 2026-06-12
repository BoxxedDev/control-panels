package moth.boxxed.panels.compat;

import moth.boxxed.panels.Dashpanels;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface PanelCompat {
    void init();
    default void busInit(IEventBus bus) {}
    default void postInit() {}
    String id();

    static void loadAll(IEventBus bus) {
        ServiceLoader<PanelCompat> loader = ServiceLoader.load(PanelCompat.class, PanelCompat.class.getClassLoader());
        Iterator<PanelCompat> iterator = loader.iterator();
        Dashpanels.LOGGER.debug("Compat List: %d".formatted(loader.stream().toList().size()));
        while (iterator.hasNext()) {
            PanelCompat compat = iterator.next();
            Dashpanels.LOGGER.debug(compat.id());
            if (ModList.get().isLoaded(compat.id())) {
                compat.init();
                compat.busInit(bus);
            }
        }
    }

    static void postLoadAll() {
        ServiceLoader<PanelCompat> loader = ServiceLoader.load(PanelCompat.class, PanelCompat.class.getClassLoader());
        Iterator<PanelCompat> iterator = loader.iterator();
        while (iterator.hasNext()) {
            PanelCompat compat = iterator.next();
            Dashpanels.LOGGER.debug(compat.id());
            if (ModList.get().isLoaded(compat.id())) {
                compat.postInit();
            }
        }
    }
}
