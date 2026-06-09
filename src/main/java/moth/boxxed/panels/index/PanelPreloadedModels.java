package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.util.ColoredPreloadedModel;
import moth.boxxed.panels.util.PreLoadedModel;

public class PanelPreloadedModels {
    public static final PreLoadedModel
            SWITCH_ON = regular("switch/on"),
            SWITCH_OFF = regular("switch/off"),

            KNOB = regular("knob"),

            CONTROL_LEVER_BASE = regular("control_lever/base"),
            CONTROL_LEVER_HANDLE = regular("control_lever/handle"),
            CONTROL_LEVER_INDICATOR = regular("control_lever/indicator"),

            INDICATOR_BULB_BASE = regular("indicator_bulb/base"),

            MOMENTARY_SWITCH_BASE = regular("momentary_switch/base"),
            MOMENTARY_SWITCH_BUTTON = regular("momentary_switch/button"),

            JOYSTICK_BASE = regular("joystick/base"),
            JOYSTICK_BETWEEN = regular("joystick/between"),
            JOYSTICK_STICK = regular("joystick/stick"),
            JOYSTICK_TRIGGER = regular("joystick/trigger");

    //Too lazy rn to find a way to change the texture automatically on render
    public static final ColoredPreloadedModel
            INDICATOR_BULB_ON = regularColored("indicator_bulb/bulb_on"),
            INDICATOR_BULB_OFF = regularColored("indicator_bulb/bulb_off");

    private static PreLoadedModel regular(String name) {
        return PreLoadedModel.create(Dashpanels.path("block/" + name));
    }

    private static ColoredPreloadedModel regularColored(String name) {
        return new ColoredPreloadedModel(Dashpanels.path("block/" + name));
    }

    public static void init() {}
}
