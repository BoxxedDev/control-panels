package moth.boxxed.panels.event;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.network.connecting_panels.ConnectingModulesNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = ControlPanels.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ControlPanelsServerEvents {
    @SubscribeEvent
    public static void onPostServerTick(ServerTickEvent.Post event) {

    }
}
