package moth.boxxed.panels.content.panel.modules.momentary_switch;

import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;

public class MomentarySwitchHoldInteraction extends ModuleHoldInteraction<MomentarySwitchModule> {
    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        return true;
    }

    @Override
    public void start() {
        this.update(1);
    }

    @Override
    public void stop() {
        this.update(0);
    }
}
