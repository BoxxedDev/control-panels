package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.modules.SwitchModule;
import moth.boxxed.panels.content.panel.modules.control_lever.ControlLeverModule;
import moth.boxxed.panels.content.panel.modules.knob.KnobModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelModules {
    public static final DeferredRegister<ModuleType<?>> MODULES = DeferredRegister.create(ModulesRegistry.MODULE_REGISTRY, Dashpanels.MOD_ID);

    public static final Supplier<ModuleType<SwitchModule>> SWITCH =
            MODULES.register("switch", () -> new ModuleType<>(SwitchModule::new, PanelItems.SWITCH_MODULE.get()));
    public static final Supplier<ModuleType<KnobModule>> KNOB =
            MODULES.register("knob", () -> new ModuleType<>(KnobModule::new, PanelItems.KNOB_MODULE.get()));
    public static final Supplier<ModuleType<ControlLeverModule>> CONTROL_LEVER =
            MODULES.register("control_lever", () -> new ModuleType<>(ControlLeverModule::new, PanelItems.CONTROL_LEVER_MODULE.get()));

    public static void register(IEventBus bus) {
        MODULES.register(bus);
    }
}
