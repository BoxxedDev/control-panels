package moth.boxxed.panels.compat.sable;

import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.compat.PanelCompat;
import moth.boxxed.panels.compat.sable.modules.NavballModule;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.index.PanelModules;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

public class PanelSableRegistries implements PanelCompat {
    public static Supplier<ModuleType<NavballModule>> NAVBALL;
    public static DeferredItem<Item> NAVBALL_MODULE;

    @Override
    public void init() {
        NAVBALL_MODULE = PanelItems.moduleItem("navball");
        NAVBALL = PanelModules.MODULES.register(
                "navball", () -> new ModuleType<>(NavballModule::new, NAVBALL_MODULE)
        );
    }

    @Override
    public String id() {
        return "sable";
    }
}
