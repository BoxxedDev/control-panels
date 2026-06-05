package moth.boxxed.panels.api.registry;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.ModuleType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModulesRegistry {
    private static final ResourceKey<Registry<ModuleType<?>>> MODULE_REGISTRY_KEY = ResourceKey.createRegistryKey(Dashpanels.path("panel_modules"));
    public static final Registry<ModuleType<?>> MODULE_REGISTRY = new RegistryBuilder<>(MODULE_REGISTRY_KEY).create();
}
