package moth.boxxed.panels.event;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.PanelSkinsServerManager;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.compat.create.panel_link.screen.PanelLinkScreen;
import moth.boxxed.panels.content.cable.stripped.screen.StrippedCableScreen;
import moth.boxxed.panels.content.panel.screen.PanelScreen;
import moth.boxxed.panels.datagen.*;
import moth.boxxed.panels.index.PanelKeybinds;
import moth.boxxed.panels.index.PanelMenuTypes;
import moth.boxxed.panels.network.handler.ServerPayloadHandler;
import moth.boxxed.panels.network.packet.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Dashpanels.MOD_ID)
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
        registrar.playBidirectional(
                ConfigureStrippedCablePacket.TYPE,
                ConfigureStrippedCablePacket.STREAM_CODEC,
                ServerPayloadHandler::handleStrippedConfig
        );
        registrar.playToServer(
                SetPlayerSlotPacket.TYPE,
                SetPlayerSlotPacket.STREAM_CODEC,
                ServerPayloadHandler::handleSetPlayerSlot
        );
        registrar.playToServer(
                SelectedModulePacket.TYPE,
                SelectedModulePacket.STREAM_CODEC,
                ServerPayloadHandler::handleSelectedModule
        );
        //Compat packet
        if (ModList.get().isLoaded("create"))
            registrar.playToServer(
                    PanelLinkSaveEntriesPacket.TYPE,
                    PanelLinkSaveEntriesPacket.STREAM_CODEC,
                    ServerPayloadHandler::handleSavePanelLink
            );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(PanelMenuTypes.PANEL.get(), PanelScreen::new);
        event.register(PanelMenuTypes.STRIPPED_CONFIG.get(), StrippedCableScreen::new);
        if (ModList.get().isLoaded("create"))
            event.register(PanelCreateRegistries.PANEL_LINK_MENU.get(), PanelLinkScreen::new);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        //Client
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
                new PanelModelProviders.Block(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new PanelLangProvider(output)
        );

        //Server
        generator.addProvider(
                event.includeServer(),
                new PanelTagProviders.Items(output, lookupProvider, existingFileHelper)
        );
        generator.addProvider(
                event.includeServer(),
                new PanelTagProviders.Blocks(output, lookupProvider, existingFileHelper)
        );
        generator.addProvider(
                event.includeServer(),
                new PanelLootProvider(output, lookupProvider)
        );
        generator.addProvider(
                event.includeServer(),
                new PanelRecipeProvider(output, lookupProvider)
        );
    }

    @SubscribeEvent
    public static void tabContents(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTab() == PanelCreativeTabs.PANEL_TAB.get()) {
//            event.accept(PanelBlocks.CONTROL_PANEL);
//            event.accept(PanelBlocks.CABLE);
//            event.accept(PanelItems.CABLE_STRIPPER);
//        }
//        if (event.getTab() == PanelCreativeTabs.MODULES_TAB.get()) {
//            for (DeferredHolder<ModuleType<?>, ? extends ModuleType<?>> holder : PanelModules.MODULES.getEntries()) {
//                event.accept(holder.get().associatedItem);
//            }
//        }
//
//        Set<ItemLike> itemLikesToAdd = new LinkedHashSet<>();
//        for (Map.Entry<Supplier<CreativeModeTab>, Set<ItemLike>> entry : PanelCreativeTabs.creativeItemMap.entrySet()) {
//            if (entry.getKey().get() == event.getTab())
//                itemLikesToAdd.addAll(entry.getValue());
//        }
//        for (ItemLike itemLike : itemLikesToAdd) {
//            event.accept(itemLike);
//        }
    }

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyMapping : PanelKeybinds.MAPPINGS) {
            event.register(keyMapping);
        }
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PanelSkinsServerManager.ReloadListener.INSTANCE);
    }
}
