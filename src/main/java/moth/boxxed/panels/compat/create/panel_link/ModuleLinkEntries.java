package moth.boxxed.panels.compat.create.panel_link;

import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;

public class ModuleLinkEntries {
    public static class ModuleEntry implements IRedstoneLinkable {
        @Override
        public int getTransmittedStrength() {
            return 0;
        }

        @Override
        public void setReceivedStrength(int power) {

        }

        @Override
        public boolean isListening() {
            return false;
        }

        @Override
        public boolean isAlive() {
            return false;
        }

        @Override
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return null;
        }

        @Override
        public BlockPos getLocation() {
            return null;
        }
    }
}
