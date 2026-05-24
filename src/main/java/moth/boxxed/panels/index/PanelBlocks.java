package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlock;
import moth.boxxed.panels.content.panel.PanelBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ControlPanels.MOD_ID);

    public static final DeferredBlock<PanelBlock> CONTROL_PANEL =
            registerBlock("control_panel",
                    () -> new PanelBlock(
                            BlockBehaviour.Properties.of()
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<CableBlock> CABLE =
            registerBlock("cable",
                    () -> new CableBlock(
                            BlockBehaviour.Properties.of()
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<StrippedCableBlock> STRIPPED_CABLE =
            registerBlock("stripped_cable",
                    () -> new StrippedCableBlock(
                            BlockBehaviour.Properties.of()
                                    .noOcclusion()
                    )
            );

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> ret = BLOCKS.register(name, block);
        PanelItems.blockItem(name, ret);
        return ret;
    }

    public static class DeferredBlockAndItem<B extends Block, I extends BlockItem> {

    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
