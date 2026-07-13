package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlock;
import moth.boxxed.panels.content.panel.normal.PanelBlock;
import moth.boxxed.panels.content.panel.wall.WallPanelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Dashpanels.MOD_ID);

    public static final DeferredBlock<PanelBlock> CONTROL_PANEL =
            registerBlock("control_panel",
                    () -> new PanelBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(1f, 20f)
                                    .sound(SoundType.NETHERITE_BLOCK)
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .noOcclusion()
                    )
            );
    public static final DeferredBlock<WallPanelBlock> WALL_CONTROL_PANEL =
            registerBlock("wall_control_panel",
                    () -> new WallPanelBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(1f, 20f)
                                    .sound(SoundType.NETHERITE_BLOCK)
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<CableBlock> CABLE =
            registerBlock("cable",
                    () -> new CableBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(0.5f, 10f)
                                    .sound(SoundType.COPPER)
                                    .mapColor(MapColor.COLOR_BLACK)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<StrippedCableBlock> STRIPPED_CABLE =
            registerBlockWithoutItem("stripped_cable",
                    () -> new StrippedCableBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(0.5f, 10f)
                                    .sound(SoundType.COPPER)
                                    .lootFrom(CABLE)
                                    .mapColor(MapColor.COLOR_ORANGE)
                                    .noOcclusion()
                    )
            );

    private static <T extends Block> DeferredBlock<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> ret = BLOCKS.register(name, block);
        PanelItems.blockItem(name, ret);
        return ret;
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
