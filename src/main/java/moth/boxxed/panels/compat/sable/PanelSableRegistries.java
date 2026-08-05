package moth.boxxed.panels.compat.sable;

import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.wiki.WikiPage;
import moth.boxxed.panels.api.wiki.WikiableEntries;
import moth.boxxed.panels.compat.PanelCompat;
import moth.boxxed.panels.compat.sable.modules.NavballModule;
import moth.boxxed.panels.index.PanelItems;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelWikiCategories;
import moth.boxxed.panels.index.PanelWikiPages;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

public class PanelSableRegistries implements PanelCompat {
    public static Supplier<ModuleType<NavballModule>> NAVBALL;
    public static DeferredItem<Item> NAVBALL_MODULE;

    @Override
    public void init() {
        NAVBALL_MODULE = PanelItems.item("navball");
        NAVBALL = PanelModules.MODULES.register(
                "navball", () -> new ModuleType<>(NavballModule::new, NAVBALL_MODULE.get())
        );

        if (FMLLoader.getDist().isClient()) {
            PanelWikiPages.MODULES.addParagraph("• dashpanels:navball");

            WikiableEntries.register(NAVBALL_MODULE.getId(),
                    WikiPage.of(NAVBALL_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("The navball is a module meant to be put on sable sublevels.")
            );
        }
    }

    @Override
    public String id() {
        return "sable";
    }
}
