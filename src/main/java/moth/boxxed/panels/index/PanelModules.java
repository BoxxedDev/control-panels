package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.modules.SwitchModule;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class PanelModules {
    public static final DeferredRegister<ModuleType<?>> MODULES = DeferredRegister.create(ModulesRegistry.MODULE_REGISTRY, ControlPanels.MOD_ID);

    public static final Supplier<ModuleType<SwitchModule>> SWITCH =
            MODULES.register("switch", () -> new ModuleType<>(SwitchModule::new, PanelItems.SWITCH_MODULE.get()));

    public static void register(IEventBus bus) {
        MODULES.register(bus);
    }
}
