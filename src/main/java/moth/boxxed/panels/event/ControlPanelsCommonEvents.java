package moth.boxxed.panels.event;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.skin.PanelSkinsServerManager;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.compat.create.panel_link.screen.PanelLinkScreen;
import moth.boxxed.panels.content.cable.stripped.screen.StrippedCableScreen;
import moth.boxxed.panels.datagen.*;
import moth.boxxed.panels.index.PanelKeybinds;
import moth.boxxed.panels.index.PanelMenuTypes;
import moth.boxxed.panels.index.PanelTags;
import moth.boxxed.panels.network.handler.ClientPayloadHandler;
import moth.boxxed.panels.network.handler.ServerPayloadHandler;
import moth.boxxed.panels.network.packet.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
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
                ConfigureModulePacket.TYPE,
                ConfigureModulePacket.STREAM_CODEC,
                ServerPayloadHandler::handleConfigureModule
        );
        registrar.playToServer(
                DefaultModuleUpdatePacket.TYPE,
                DefaultModuleUpdatePacket.STREAM_CODEC,
                ServerPayloadHandler::handleDefaultUpdate
        );
        registrar.playToServer(
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
        registrar.playToClient(
                OpenPaintWheelPacket.TYPE,
                OpenPaintWheelPacket.STREAM_CODEC,
                ClientPayloadHandler::handleOpenPaintWheel
        );
        registrar.playToServer(
                SetPanelSkinPacket.TYPE,
                SetPanelSkinPacket.STREAM_CODEC,
                ServerPayloadHandler::handleSetPanelSkin
        );
        registrar.playToServer(
                PlaceModulePacket.TYPE,
                PlaceModulePacket.STREAM_CODEC,
                ServerPayloadHandler::handlePlaceModule
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

//    @SubscribeEvent
//    public static void useItemOnBlock(UseItemOnBlockEvent event) {
//        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK)
//            return;
//
//        UseOnContext context = event.getUseOnContext();
//        BlockPos pos = context.getClickedPos();
//        Level level = context.getLevel();
//        Player player = context.getPlayer();
//        BlockState state = level.getBlockState(pos);
//        ItemStack itemInHand = player.getMainHandItem();
//
//        if (itemInHand.is(PanelTags.Items.WRENCH) &&
//                state.getBlock() instanceof AbstractPanelBlock &&
//                level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe) {
//            Dashpanels.LOGGER.debug("{} | C: {}", event.getUsePhase(), level.isClientSide);
//            if (player.isShiftKeyDown()) {
//                if (pbe.removeSelectedModule(player)) {
//                    event.cancelWithResult(ItemInteractionResult.SUCCESS);
//                }
//            } else {
//                if (pbe.openConfigureScreen(level, pos, player)) {
//                    event.cancelWithResult(ItemInteractionResult.SUCCESS);
//                }
//            }
//        }
//    }
}
