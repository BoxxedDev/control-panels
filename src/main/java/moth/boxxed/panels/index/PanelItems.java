package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.content.cable.stripper.CableStripperItem;
import moth.boxxed.panels.content.paintbrush.PaintbrushItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PanelItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Dashpanels.MOD_ID);

    public static final DeferredItem<CableStripperItem> CABLE_STRIPPER = ITEMS.register("cable_stripper",
            () -> new CableStripperItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<PaintbrushItem> PAINT_BRUSH = ITEMS.register("paint_brush",
            () -> new PaintbrushItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> WRENCH = ITEMS.register("wrench",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SWITCH_MODULE = item("switch");
    public static final DeferredItem<Item> KNOB_MODULE = item("knob");
    public static final DeferredItem<Item> CONTROL_LEVER_MODULE = item("control_lever");
    public static final DeferredItem<Item> INDICATOR_BULB_MODULE = item("indicator_bulb");
    public static final DeferredItem<Item> MOMENTARY_SWITCH_MODULE = item("momentary_switch");
    public static final DeferredItem<Item> JOYSTICK_MODULE = item("joystick");
    public static final DeferredItem<Item> LABEL_MODULE = item("label");
    public static final DeferredItem<Item> SEVEN_SEGMENT_MODULE = item("seven_segment");
    public static final DeferredItem<Item> PUSH_BUTTON_MODULE = item("push_button");
    public static final DeferredItem<Item> KEY_SWITCH_MODULE = item("key_switch");

    public static final DeferredItem<Item> KEY_ITEM = ITEMS.register("key",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static DeferredItem<Item> item(String name) {
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
