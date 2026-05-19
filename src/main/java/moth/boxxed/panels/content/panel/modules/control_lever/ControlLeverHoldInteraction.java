package moth.boxxed.panels.content.panel.modules.control_lever;

import moth.boxxed.panels.network.DefaultModuleUpdatePacket;
import moth.boxxed.panels.util.interaction.ModuleHoldInteraction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class ControlLeverHoldInteraction extends ModuleHoldInteraction<ControlLeverModule> {
    private float val = 0;
    private int oldSignal = 0;
    private int signal = 0;

    private ControlLeverModule leverModule;
    private Level level;

    @Override
    public void startHold(Level level, Player player, ControlLeverModule module) {
        super.startHold(level, player, module);
        this.signal = module.getSignal();
        this.val = this.signal/15f;
        this.leverModule = module;
        this.level = level;
    }

    @Override
    public boolean activeMouseMove(double yaw, double pitch) {
        this.val -= (float) (pitch/180f);
        this.val = Math.clamp(this.val, 0, 1);
        this.signal = Math.clamp(Math.round(this.val*15), 0, 15);
        if (this.oldSignal != this.signal) {
            PacketDistributor.sendToServer(new DefaultModuleUpdatePacket(this.leverModule.getParentPos(), this.leverModule.getName(), this.signal));
        }
        this.oldSignal = this.signal;
        return true;
    }
}
