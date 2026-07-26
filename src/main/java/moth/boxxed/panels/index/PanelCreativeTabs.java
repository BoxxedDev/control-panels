package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
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
import java.util.List;
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
        CustomSectionTextured defaultSection = new CustomSectionTextured(Dashpanels.path("dashpanels"));
        defaultSection.setTitle(Component.translatable("creativetab.dashpanels.dashpanels"));

        defaultSection.addItemTag(PanelTags.Items.PANELS);
        defaultSection.add(PanelBlocks.CABLE);
        if (ModList.get().isLoaded("create")) {
            defaultSection.add(PanelCreateRegistries.PANEL_LINK);
        }

        defaultSection.add(Items.AIR);
        
        defaultSection.add(PanelItems.CABLE_STRIPPER);
        defaultSection.add(PanelItems.WRENCH);
        defaultSection.add(PanelItems.PAINT_BRUSH);

        defaultSection.setCentered(true);
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), defaultSection);

        CustomSectionTextured modulesSection = new CustomSectionTextured(Dashpanels.path("dashpanels"));
        modulesSection.setTitle(Component.translatable("creativetab.dashpanels.modules"));
        modulesSection.setTexture(Dashpanels.path("dashpanels"));

        modulesSection.add(registryAccess -> {
            List<ItemStack> ret = new ArrayList<>();
            ModulesRegistry.MODULE_REGISTRY.entrySet().forEach(
                    entry -> ret.add(new ItemStack(entry.getValue().associatedItem))
            );
            ret.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getDisplayName().getString(), b.getDisplayName().getString()));
            return ret;
        });

        modulesSection.setCentered(true);
        FancyTabSections.addSection(Dashpanels.path("dashpanels"), modulesSection);
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
        addItems();
    }
}
