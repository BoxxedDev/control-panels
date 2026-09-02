package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.util.CustomSectionTextured;
import net.mcexpanded.fancytabsections.FancyTabSections;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PanelCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Dashpanels.MOD_ID);

    public static final Supplier<CreativeModeTab> PANEL_TAB = TABS.register(
            "dashpanels",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.control_panel"))
                    .icon(PanelBlocks.CONTROL_PANEL::toStack)
                    .displayItems((a, b) -> {})
                    .build()
    );

    public static final CustomSectionTextured MAIN_SECTION = (CustomSectionTextured) new CustomSectionTextured(Dashpanels.path("dashpanels"))
            .setTitle(Component.translatable("creativetab.dashpanels.dashpanels"));
    public static final CustomSectionTextured MODULES_SECTION = (CustomSectionTextured) new CustomSectionTextured(Dashpanels.path("modules"))
            .setTitle(Component.translatable("creativetab.dashpanels.modules"))
            .setTexture(Dashpanels.path("dashpanels"));
    public static final CustomSectionTextured TOOLS_SECTION = (CustomSectionTextured) new CustomSectionTextured(Dashpanels.path("extras"))
            .setTitle(Component.translatable("creativetab.dashpanels.tools"))
            .setTexture(Dashpanels.path("dashpanels"));

    static {
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), MAIN_SECTION);
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), TOOLS_SECTION);
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), MODULES_SECTION);
    }

    public static void addItems() {
        MAIN_SECTION.addItemTag(PanelTags.Items.PANELS);
        MAIN_SECTION.add(PanelBlocks.CABLE);
        if (ModList.get().isLoaded("create")) {
            MAIN_SECTION.add(PanelCreateRegistries.PANEL_LINK);
        }
        
        TOOLS_SECTION.add(PanelItems.CABLE_STRIPPER);
        TOOLS_SECTION.add(PanelItems.WRENCH);
        TOOLS_SECTION.add(PanelItems.PAINT_BRUSH);

        TOOLS_SECTION.add(Items.AIR);

        TOOLS_SECTION.add(PanelItems.KEY_ITEM);
        TOOLS_SECTION.add(PanelItems.KEY_CHAIN);
//        TOOLS_SECTION.add(
//                () -> {
//                    ItemStack ret = new ItemStack(PanelItems.KEY_CHAIN.get());
//                    ret.set(PanelDataComponents.KEY_CHAIN_CONTENTS, KeyChainContents.EMPTY);
//                    return ret;
//                }
//        );

        MODULES_SECTION.add(registryAccess -> ModulesRegistry.MODULE_REGISTRY
                    .stream()
                    .map(type -> type.associatedItem)
                    .collect(Collectors.toSet())
                    .stream()
                    .map(ItemStack::new)
                    .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getDisplayName().getString(), b.getDisplayName().getString()))
                    .toList()
        );
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
        addItems();
    }
}
