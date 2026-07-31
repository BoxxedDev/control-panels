package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.modules.*;
import moth.boxxed.panels.content.modules.control_lever.ControlLeverModule;
import moth.boxxed.panels.content.modules.joystick.JoystickModule;
import moth.boxxed.panels.content.modules.key_switch.KeySwitchModule;
import moth.boxxed.panels.content.modules.knob.KnobModule;
import moth.boxxed.panels.content.modules.momentary_switch.MomentarySwitchModule;
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
    public static final Supplier<ModuleType<IndicatorBulbModule>> INDICATOR_BULB =
            MODULES.register("indicator_bulb", () -> new ModuleType<>(IndicatorBulbModule::new, PanelItems.INDICATOR_BULB_MODULE.get()));
    public static final Supplier<ModuleType<MomentarySwitchModule>> MOMENTARY_SWITCH =
            MODULES.register("momentary_switch", () -> new ModuleType<>(MomentarySwitchModule::new, PanelItems.MOMENTARY_SWITCH_MODULE.get()));
    public static final Supplier<ModuleType<JoystickModule>> JOYSTICK =
            MODULES.register("joystick", () -> new ModuleType<>(JoystickModule::new, PanelItems.JOYSTICK_MODULE.get()));
    public static final Supplier<ModuleType<LabelModule>> LABEL =
            MODULES.register("label", () -> new ModuleType<>(LabelModule::new, PanelItems.LABEL_MODULE.get()));
    public static final Supplier<ModuleType<SevenSegmentModule>> SEVEN_SEGMENT =
            MODULES.register("seven_segment", () -> new ModuleType<>(SevenSegmentModule::new, PanelItems.SEVEN_SEGMENT_MODULE.get()));
    public static final Supplier<ModuleType<PushButtonModule>> PUSH_BUTTON =
            MODULES.register("push_button", () -> new ModuleType<>(PushButtonModule::new, PanelItems.PUSH_BUTTON_MODULE.get()));
    public static final Supplier<ModuleType<KeySwitchModule>> KEY_SWITCH =
            MODULES.register("key_switch", () -> new ModuleType<>(KeySwitchModule::new, PanelItems.KEY_SWITCH_MODULE.get()));

    public static void register(IEventBus bus) {
        MODULES.register(bus);
    }
}
