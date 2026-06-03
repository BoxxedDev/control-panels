package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.content.cable.stripper.CableStripperItem;
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

    public static final DeferredItem<CableStripperItem> CABLE_STRIPPER = ITEMS.register("cable_stripper",
            () -> new CableStripperItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> COPPER_WIRE = ITEMS.register("copper_wire",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SWITCH_MODULE = moduleItem("switch");
    public static final DeferredItem<Item> KNOB_MODULE = moduleItem("knob");
    public static final DeferredItem<Item> CONTROL_LEVER_MODULE = moduleItem("control_lever");

    public static DeferredItem<Item> moduleItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    public static <T extends Block> DeferredItem<BlockItem> blockItem(String name, DeferredBlock<T> block) {
        return blockItem(name, block, new Item.Properties());
    }

    public static <T extends Block> DeferredItem<BlockItem> blockItem(String name, DeferredBlock<T> block, Item.Properties properties) {
        return ITEMS.registerSimpleBlockItem(name, block, properties);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
