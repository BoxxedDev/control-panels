package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import net.mcexpanded.fancytabsections.FancyTabSections;
import net.mcexpanded.fancytabsections.Section.SectionTextured;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Dashpanels.MOD_ID);

    public static final Supplier<CreativeModeTab> PANEL_TAB = TABS.register(
            "dashpanels",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.control_panel"))
                    .icon(PanelBlocks.CONTROL_PANEL::toStack)
                    .displayItems(((parameters, output) -> {}))
                    .build()
    );

    public static void addItems() {
        SectionTextured defaultSection = new SectionTextured(Dashpanels.path("dashpanels"));
        defaultSection.setTitle(Component.translatable("creativetab.dashpanels.dashpanels"));

        defaultSection.add(PanelBlocks.CONTROL_PANEL);
        defaultSection.add(PanelBlocks.CABLE);
        defaultSection.add(PanelItems.CABLE_STRIPPER);
        if (ModList.get().isLoaded("create"))
            defaultSection.add(PanelCreateRegistries.PANEL_LINK);
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), defaultSection
        );

        SectionTextured modulesSection = new SectionTextured(Dashpanels.path("dashpanels"));
        modulesSection.setTitle(Component.translatable("creativetab.dashpanels.modules"));
        modulesSection.setTexture(Dashpanels.path("dashpanels"));

        modulesSection.addItemTag(PanelTags.Items.MODULE);
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), modulesSection);
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
        addItems();
    }
}
