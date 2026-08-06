package moth.boxxed.panels.content.modules.momentary_switch;

import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import net.minecraft.nbt.CompoundTag;

public class MomentarySwitchHoldInteraction extends ModuleHoldInteraction<MomentarySwitchModule> {
    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        return true;
    }

    @Override
    public void start() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("pressed", true);
        this.update(tag);
    }

    @Override
    public void stop() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("pressed", false);
        this.update(tag);
    }
}
