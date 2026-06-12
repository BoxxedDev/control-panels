package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.compat.sable.PanelSableRegistries;
import net.mcexpanded.fancytabsections.FancyTabSections;
import net.mcexpanded.fancytabsections.creativetab.ConglomerateOfItems;
import net.mcexpanded.fancytabsections.creativetab.SectionTextured;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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

    public static final Map<Supplier<CreativeModeTab>, Set<ItemLike>> creativeItemMap = new HashMap<>();
    public static void addContentTo(Supplier<CreativeModeTab> tab, ItemLike item) {
        Set<ItemLike> set = creativeItemMap.computeIfAbsent(tab, $ -> new LinkedHashSet<>());
        set.add(item);
    }

    public static void addItems() {
        ConglomerateOfItems base = new ConglomerateOfItems()
                .add(PanelBlocks.CONTROL_PANEL)
                .add(PanelBlocks.CABLE)
                .add(PanelItems.CABLE_STRIPPER);
        if (ModList.get().isLoaded("create"))
            base.add(PanelCreateRegistries.PANEL_LINK);
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), SectionTextured.of(
                Dashpanels.path("dashpanels"),
                Component.translatable("creativetab.dashpanels.dashpanels"),
                0xFFFFFF,
                base
        ));

        ConglomerateOfItems modules = new ConglomerateOfItems()
                .add(PanelItems.SWITCH_MODULE)
                .add(PanelItems.KNOB_MODULE)
                .add(PanelItems.CONTROL_LEVER_MODULE)
                .add(PanelItems.INDICATOR_BULB_MODULE)
                .add(PanelItems.MOMENTARY_SWITCH_MODULE)
                .add(PanelItems.JOYSTICK_MODULE)
                .add(PanelItems.LABEL_MODULE)
                .add(PanelItems.SEVEN_SEGMENT_MODULE);
        if (ModList.get().isLoaded("sable"))
            modules.add(PanelSableRegistries.NAVBALL_MODULE);
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), new SectionTextured(
                Dashpanels.path("modules"),
                Component.translatable("creativetab.dashpanels.modules"),
                Dashpanels.path("textures/gui/fancy_tab_section/dashpanels.png"),
                0xFFFFFF,
                modules
        ));
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
        addItems();
    }
}
