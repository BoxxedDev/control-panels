package moth.boxxed.panels;

import moth.boxxed.panels.content.paintbrush.ColorPaletteStorage;
import moth.boxxed.panels.event.ControlPanelsClientEvents;
import moth.boxxed.panels.index.PanelKeybinds;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.index.PanelWikiPages;
import moth.boxxed.panels.util.PreLoadedModelHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Dashpanels.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Dashpanels.MOD_ID, value = Dist.CLIENT)
public class DashpanelsClient {
    public static final ColorPaletteStorage PALETTE_STORAGE = new ColorPaletteStorage();

    public DashpanelsClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        PanelPreloadedModels.init();
        PanelKeybinds.init();
        PanelWikiPages.init();

        modEventBus.addListener(PreLoadedModelHandler::registerAdditional);
        modEventBus.addListener(PreLoadedModelHandler::bakingCompleted);

        PALETTE_STORAGE.createDefault();
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }
}
