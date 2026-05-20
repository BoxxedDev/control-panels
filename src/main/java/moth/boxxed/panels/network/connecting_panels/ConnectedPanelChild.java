package moth.boxxed.panels.network.connecting_panels;

import net.minecraft.core.Direction;

public interface ConnectedPanelChild {
     ConnectingPanelsNetwork network = null;
     
     boolean filterConnectionSide(Direction direction);
}
