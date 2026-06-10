package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.modules.IndicatorBulbModule;
import moth.boxxed.panels.content.panel.modules.LabelModule;
import moth.boxxed.panels.content.panel.modules.SwitchModule;
import moth.boxxed.panels.content.panel.modules.control_lever.ControlLeverModule;
import moth.boxxed.panels.content.panel.modules.joystick.JoystickModule;
import moth.boxxed.panels.content.panel.modules.knob.KnobModule;
import moth.boxxed.panels.content.panel.modules.momentary_switch.MomentarySwitchModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelModules {
    public static final DeferredRegister<ModuleType<?>> MODULES = DeferredRegister.create(ModulesRegistry.MODULE_REGISTRY, Dashpanels.MOD_ID);

    public static final Supplier<ModuleType<SwitchModule>> SWITCH =
            MODULES.register("switch", () -> new ModuleType<>(SwitchModule::new, PanelItems.SWITCH_MODULE));
    public static final Supplier<ModuleType<KnobModule>> KNOB =
            MODULES.register("knob", () -> new ModuleType<>(KnobModule::new, PanelItems.KNOB_MODULE));
    public static final Supplier<ModuleType<ControlLeverModule>> CONTROL_LEVER =
            MODULES.register("control_lever", () -> new ModuleType<>(ControlLeverModule::new, PanelItems.CONTROL_LEVER_MODULE));
    public static final Supplier<ModuleType<IndicatorBulbModule>> INDICATOR_BULB =
            MODULES.register("indicator_bulb", () -> new ModuleType<>(IndicatorBulbModule::new, PanelItems.INDICATOR_BULB_MODULE));
    public static final Supplier<ModuleType<MomentarySwitchModule>> MOMENTARY_SWITCH =
            MODULES.register("momentary_switch", () -> new ModuleType<>(MomentarySwitchModule::new, PanelItems.MOMENTARY_SWITCH_MODULE));
    public static final Supplier<ModuleType<JoystickModule>> JOYSTICK =
            MODULES.register("joystick", () -> new ModuleType<>(JoystickModule::new, PanelItems.JOYSTICK_MODULE));
    public static final Supplier<ModuleType<LabelModule>> LABEL =
            MODULES.register("label", () -> new ModuleType<>(LabelModule::new, PanelItems.LABEL_MODULE));

    public static void register(IEventBus bus) {
        MODULES.register(bus);
    }
}
