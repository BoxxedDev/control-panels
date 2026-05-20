package moth.boxxed.panels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import moth.boxxed.panels.compat.PanelCompat;
import moth.boxxed.panels.compat.computercraft.CCPeripherals;
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

//TODO: Add javadocs to everything
@Mod(ControlPanels.MOD_ID)
public class ControlPanels {
    public static final String MOD_ID = "panels";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ControlPanels(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        PanelItems.register(modEventBus);
        PanelBlocks.register(modEventBus);
        PanelBlockEntities.register(modEventBus);
        PanelModules.register(modEventBus);
        PanelMenuTypes.register(modEventBus);
        PanelCreativeTabs.register(modEventBus);

        PanelTags.init();
        PanelCompat.loadAll();

        NeoForge.EVENT_BUS.register(this);
        if (ModList.get().isLoaded("computercraft")) {
            modEventBus.register(CCPeripherals.class);
        }
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

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
