package moth.boxxed.panels.compat.create.panel_link;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import moth.boxxed.panels.api.module.IInput;
import moth.boxxed.panels.api.module.IOutput;
import moth.boxxed.panels.api.module.Module;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModuleLinkEntries {
    private final Map<String, ModuleEntry> entryMap = new HashMap<>();

    public void updateNetworks(Level level) {
        if (!level.isClientSide) {
            for (ModuleEntry entry : this.entryMap.values()) {
                if (entry.isAlive())
                    Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, entry);
            }
        }
    }



    public static class ModuleEntry implements IRedstoneLinkable {
        private final RedstoneLinkNetworkHandler.Frequency first;
        private final RedstoneLinkNetworkHandler.Frequency second;

        private BlockPos pos;
        private final Module module;

        private final boolean input;
        private final boolean output;

        public ModuleEntry(RedstoneLinkNetworkHandler.Frequency first, RedstoneLinkNetworkHandler.Frequency second, final Module module, BlockPos pos) {
            if (first == null)
                first = RedstoneLinkNetworkHandler.Frequency.EMPTY;
            if (second == null)
                second = RedstoneLinkNetworkHandler.Frequency.EMPTY;

            this.first = first;
            this.second= second;

            this.pos = pos;
            this.module = module;

            this.input = module instanceof IInput;
            this.output = module instanceof IOutput;
        }

        @Override
        public int getTransmittedStrength() {
            if (this.input)
                return ((IInput) this.module).getAnalog();
            return 0;
        }

        @Override
        public void setReceivedStrength(int power) {
            if (this.output)
                ((IOutput) this.module).setAnalog(power);
        }

        @Override
        public boolean isListening() {
            return this.output;
        }

        @Override
        public boolean isAlive() {
            return (this.input && ((IInput) this.module).getAnalog() > 0) || this.output;
        }

        @Override
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return Couple.create(this.first, this.second);
        }

        @Override
        public BlockPos getLocation() {
            return this.pos;
        }
    }
}
