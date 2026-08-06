package moth.boxxed.panels.content.modules.key_switch;

import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import net.minecraft.nbt.CompoundTag;

public class KeySwitchHoldInteraction extends ModuleHoldInteraction<KeySwitchModule> {
    private float turn;

    @Override
    public void start() {
        this.turn = this.module.turned ? 1 : 0;
    }

    @Override
    public void stop() {
        float turn;
        boolean turned;

        if (this.module.togglable.get()) {
            turned = this.turn >= 0.5f;
            turn = turned ? 1 : 0;
        } else {
            turned = false;
            turn = 0;
        }

        CompoundTag tag = new CompoundTag();
        tag.putFloat("turn", turn);
        tag.putBoolean("turned", turned);
        this.update(tag);
    }

    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        float oldTurn = this.turn;
        this.turn = (float) Math.clamp(this.turn+(yaw/720f), 0, 1);
        if (this.turn != oldTurn) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("turn", this.turn);
            tag.putBoolean("turned", this.turn >= 0.5f);
            this.update(tag);
        }
        return true;
    }
}
