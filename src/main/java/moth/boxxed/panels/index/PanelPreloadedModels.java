package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
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
            JOYSTICK_TRIGGER = regular("joystick/trigger"),

            NAVBALL_BASE = regular("navball/base"),
            NAVBALL = regular("navball/ball"),

            LABEL = regular("label"),

            SEVEN_SEGMENT = regular("seven_segment"),

            INDICATOR_BULB_ON = regular("indicator_bulb/bulb_on"),
            INDICATOR_BULB_OFF = regular("indicator_bulb/bulb_off"),

            PUSH_BUTTON_BASE = regular("push_button/base"),
            PUSH_BUTTON = regular("push_button/button")
                    ;

    private static PreLoadedModel regular(String name) {
        return PreLoadedModel.create(Dashpanels.path("block/" + name));
    }

    public static void init() {}
}
