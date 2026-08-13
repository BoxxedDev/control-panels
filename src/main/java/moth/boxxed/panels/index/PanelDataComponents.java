package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.content.modules.key_switch.BoundModule;
import moth.boxxed.panels.content.modules.key_switch.KeyChainContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PanelDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Dashpanels.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BoundModule>> BOUND_MODULE = DATA_COMPONENTS.registerComponentType(
            "bound_module",
            builder -> builder
                    .persistent(BoundModule.CODEC)
                    .networkSynchronized(BoundModule.STREAM_CODEC)
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KeyChainContents>> KEY_CHAIN_CONTENTS = DATA_COMPONENTS.registerComponentType(
            "key_chain_contents",
            builder -> builder
                    .persistent(KeyChainContents.CODEC)
                    .networkSynchronized(KeyChainContents.STREAM_CODEC)
    );

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
