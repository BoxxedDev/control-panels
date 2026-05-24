package moth.boxxed.panels.api.network.connecting_panels;

import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectingModulesNetworkManager {
    public static final Map<LevelAccessor, Map<UUID, ConnectingModulesNetwork>> ALL = new HashMap<>();

    @SubscribeEvent
    public static void onLoad(LevelEvent.Load event) {
        ALL.put(event.getLevel(), new HashMap<>());
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        ALL.remove(event.getLevel());
    }
}
