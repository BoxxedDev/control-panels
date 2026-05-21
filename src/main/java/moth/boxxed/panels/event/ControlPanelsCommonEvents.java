package moth.boxxed.panels.event;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.screen.PanelScreen;
import moth.boxxed.panels.datagen.PanelBlockStateProvider;
import moth.boxxed.panels.datagen.PanelLangProvider;
import moth.boxxed.panels.datagen.PanelModelProviders;
import moth.boxxed.panels.datagen.PanelTagProviders;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelCreativeTabs;
import moth.boxxed.panels.index.PanelMenuTypes;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.network.packet.DefaultModuleUpdatePacket;
import moth.boxxed.panels.network.packet.SavePanelModulesPacket;
import moth.boxxed.panels.network.packet.ServerPayloadHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ControlPanels.MOD_ID)
public class ControlPanelsCommonEvents {
    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(ModulesRegistry.MODULE_REGISTRY);
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("control_panels_1");
        registrar.playToServer(
                SavePanelModulesPacket.TYPE,
                SavePanelModulesPacket.STREAM_CODEC,
                ServerPayloadHandler::handleSavePanelModules
        );
        registrar.playToServer(
                DefaultModuleUpdatePacket.TYPE,
                DefaultModuleUpdatePacket.STREAM_CODEC,
                ServerPayloadHandler::handleDefaultUpdate
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(PanelMenuTypes.PANEL.get(), PanelScreen::new);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(
                event.includeClient(),
                new PanelBlockStateProvider(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new PanelModelProviders.Item(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new PanelLangProvider(output)
        );
        generator.addProvider(
                event.includeServer(),
                new PanelTagProviders.Item(output, lookupProvider, existingFileHelper)
        );
    }

    @SubscribeEvent
    public static void tabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == PanelCreativeTabs.PANEL_TAB.get()) {
            event.accept(PanelBlocks.CONTROL_PANEL);
        }
        if (event.getTab() == PanelCreativeTabs.MODULES_TAB.get()) {
            for (DeferredHolder<ModuleType<?>, ? extends ModuleType<?>> holder : PanelModules.MODULES.getEntries()) {
                event.accept(holder.get().associatedItem);
            }
        }
    }
}
