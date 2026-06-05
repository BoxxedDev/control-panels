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
            CONTROL_LEVER_INDICATOR = regular("control_lever/indicator");

    private static PreLoadedModel regular(String name) {
        return PreLoadedModel.create(Dashpanels.path("block/" + name));
    }

    public static void init() {}
}
