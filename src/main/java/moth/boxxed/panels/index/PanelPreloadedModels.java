package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.ControlPanelsClient;
import moth.boxxed.panels.util.PreLoadedModel;

public class PanelPreloadedModels {
    public static final PreLoadedModel
            SWITCH_ON = regular("switch/on"),
            SWITCH_OFF = regular("switch/off");

    private static PreLoadedModel regular(String name) {
        return PreLoadedModel.create(ControlPanels.path("block/" + name));
    }

    public static void init() {}
}
