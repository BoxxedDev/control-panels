package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;

public class PanelCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, ControlPanels.MOD_ID);

    public static final Supplier<CreativeModeTab> PANEL_TAB = TABS.register(
            "control_panel",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.control_panel"))
                    .icon(PanelBlocks.CONTROL_PANEL::toStack)
                    .build()
    );
    public static final Supplier<CreativeModeTab> MODULES_TAB = TABS.register(
            "control_panel_modules",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.control_panel_modules"))
                    .icon(PanelItems.SWITCH_MODULE::toStack)
                    .build()
    );

    public static final Map<Supplier<CreativeModeTab>, Set<ItemLike>> creativeItemMap = new HashMap<>();
    public static void addContentTo(Supplier<CreativeModeTab> tab, ItemLike item) {
        Set<ItemLike> set = creativeItemMap.computeIfAbsent(tab, $ -> new LinkedHashSet<>());
        set.add(item);
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
