package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.content.cable.CableBlockEntity;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Calendar;
import java.util.function.Supplier;

public class PanelBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ControlPanels.MOD_ID);

    public static final Supplier<BlockEntityType<PanelBlockEntity>> PANEL =
            BLOCK_ENTITY_TYPES.register("panel", () -> BlockEntityType.Builder.of(
                    PanelBlockEntity::new, PanelBlocks.CONTROL_PANEL.get()
            ).build(null)
            );

    public static final Supplier<BlockEntityType<CableBlockEntity>> CABLE =
            BLOCK_ENTITY_TYPES.register("cable", () -> BlockEntityType.Builder.of(
                            CableBlockEntity::new, PanelBlocks.CABLE.get()
                    ).build(null)
            );

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
