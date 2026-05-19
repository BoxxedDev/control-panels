package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.ControlPanelsClient;
import moth.boxxed.panels.util.PreLoadedModel;

public class PanelPreloadedModels {
    public static final PreLoadedModel
            SWITCH_ON = regular("switch/on"),
            SWITCH_OFF = regular("switch/off"),

            KNOB = regular("knob"),

            CONTROL_LEVER = regular("control_lever/lever"),
            CONTROL_LEVER_BASE = regular("control_lever/base")
                    ;

    private static PreLoadedModel regular(String name) {
        return PreLoadedModel.create(ControlPanels.path("block/" + name));
    }

    public static void init() {}
}
