package moth.boxxed.panels.index;

import moth.boxxed.panels.content.panel.modules.control_lever.ControlLeverHoldInteraction;
import moth.boxxed.panels.content.panel.modules.knob.KnobHoldInteraction;
import moth.boxxed.panels.util.interaction.ModuleHoldInteraction;

import java.util.HashSet;
import java.util.Set;

public class PanelHoldInteractions {
    public static final Set<ModuleHoldInteraction<?>> INTERACTIONS = new HashSet<>();

    public static KnobHoldInteraction KNOB = register(new KnobHoldInteraction());
    public static ControlLeverHoldInteraction CONTROL_LEVER = register(new ControlLeverHoldInteraction());

    private static <T extends ModuleHoldInteraction<?>> T register(T interaction) {
        INTERACTIONS.add(interaction);
        return interaction;
    }
}
