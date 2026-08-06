package moth.boxxed.panels.index;

import moth.boxxed.panels.content.modules.control_lever.ControlLeverHoldInteraction;
import moth.boxxed.panels.content.modules.joystick.JoystickHoldInteraction;
import moth.boxxed.panels.content.modules.key_switch.KeySwitchHoldInteraction;
import moth.boxxed.panels.content.modules.knob.KnobHoldInteraction;
import moth.boxxed.panels.content.modules.momentary_switch.MomentarySwitchHoldInteraction;

import static moth.boxxed.panels.api.module.interaction.ModuleHoldInteractionManager.register;

public class PanelHoldInteractions {
    public static KnobHoldInteraction KNOB = register(new KnobHoldInteraction());
    public static ControlLeverHoldInteraction CONTROL_LEVER = register(new ControlLeverHoldInteraction());
    public static MomentarySwitchHoldInteraction MOMENTARY_SWITCH = register(new MomentarySwitchHoldInteraction());
    public static JoystickHoldInteraction JOYSTICK = register(new JoystickHoldInteraction());
    public static KeySwitchHoldInteraction KEY_SWITCH = register(new KeySwitchHoldInteraction());
}
