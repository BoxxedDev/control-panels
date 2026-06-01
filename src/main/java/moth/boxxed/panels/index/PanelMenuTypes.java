package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.content.cable.stripped.screen.StrippedConfigMenu;
import moth.boxxed.panels.content.panel.screen.PanelMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, ControlPanels.MOD_ID);

    public static final Supplier<MenuType<PanelMenu>> PANEL =
            MENU_TYPES.register("panel", () -> IMenuTypeExtension.create(PanelMenu::new));
    public static final Supplier<MenuType<StrippedConfigMenu>> STRIPPED_CONFIG =
            MENU_TYPES.register("stripped_config", () -> IMenuTypeExtension.create(StrippedConfigMenu::new));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
    }
}
