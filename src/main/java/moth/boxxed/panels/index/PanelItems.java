package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ControlPanels.MOD_ID);

    public static final DeferredItem<Item> SWITCH_MODULE =
            ITEMS.register("switch", () -> new Item(new Item.Properties()));

    public static <T extends Block> Supplier<BlockItem> blockItem(String name, DeferredBlock<T> block) {
        return blockItem(name, block, new Item.Properties());
    }

    public static <T extends Block> Supplier<BlockItem> blockItem(String name, DeferredBlock<T> block, Item.Properties properties) {
        return ITEMS.registerSimpleBlockItem(name, block, properties);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
