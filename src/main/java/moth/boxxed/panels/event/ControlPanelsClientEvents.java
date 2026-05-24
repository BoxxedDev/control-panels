package moth.boxxed.panels.event;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import moth.boxxed.panels.index.PanelHoldInteractions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = ControlPanels.MOD_ID, value = Dist.CLIENT)
public class ControlPanelsClientEvents {
    @SubscribeEvent
    public static void guiRenderPost(RenderGuiEvent.Post event) {
        for (ModuleHoldInteraction<?> interaction : PanelHoldInteractions.INTERACTIONS) {
            if (interaction.isActive()) {
                interaction.renderGui(event.getGuiGraphics(), event.getPartialTick().getRealtimeDeltaTicks());
            }
        }
    }

    @SubscribeEvent
    public static void clientPostTick(ClientTickEvent.Post event) {
        for (ModuleHoldInteraction<?> interaction : PanelHoldInteractions.INTERACTIONS) {
            if (interaction.isActive()) {
                interaction.tick();
            }
        }
    }
}
