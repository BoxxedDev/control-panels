package moth.boxxed.panels.api.network.connecting_panels;

import java.util.UUID;

public interface INetworkMember {
    UUID getNetwork();
    void setNetwork(UUID network);
}
