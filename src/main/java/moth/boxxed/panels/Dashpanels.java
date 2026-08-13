package moth.boxxed.panels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import moth.boxxed.panels.api.network.ModulesNetworkManager;
import moth.boxxed.panels.compat.PanelCompat;
import moth.boxxed.panels.compat.computercraft.CCPeripherals;
import moth.boxxed.panels.config.PanelsClientConfig;
import moth.boxxed.panels.config.PanelsConfig;
import moth.boxxed.panels.index.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;
import org.slf4j.Logger;

//TODO: Add javadocs to everything, and refactor everything to the new name of "dashpanels"
@Mod(Dashpanels.MOD_ID)
public class Dashpanels {
    public static final String MOD_ID = "dashpanels";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Dashpanels(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        PanelCompat.loadAll(modEventBus);

        PanelItems.register(modEventBus);
        PanelBlocks.register(modEventBus);
        PanelBlockEntities.register(modEventBus);
        PanelModules.register(modEventBus);
        PanelMenuTypes.register(modEventBus);
        PanelCreativeTabs.register(modEventBus);
        PanelDataComponents.register(modEventBus);
        PanelRecipeSerializers.register(modEventBus);
        PanelSounds.register(modEventBus);

        PanelTags.init();

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ModulesNetworkManager.class);

        if (ModList.get().isLoaded("computercraft")) {
            modEventBus.register(CCPeripherals.class);
        }

        modContainer.registerConfig(ModConfig.Type.COMMON, PanelsConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, PanelsClientConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        PanelCompat.postLoadAll();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext context = event.getBuildContext();

        ConfigCommand.register(dispatcher);
    }

    public static ResourceLocation path(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }
}
