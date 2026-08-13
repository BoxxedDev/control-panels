package moth.boxxed.panels.index;

import com.google.common.collect.Maps;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.wiki.WikiPage;
import moth.boxxed.panels.api.wiki.WikiableEntries;
import moth.boxxed.panels.content.cable.stripper.CableStripperItem;
import moth.boxxed.panels.content.modules.key_switch.KeyChainContents;
import moth.boxxed.panels.content.modules.key_switch.KeyChainItem;
import moth.boxxed.panels.content.modules.key_switch.KeyItem;
import moth.boxxed.panels.content.paintbrush.PaintbrushItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final DeferredItem<Item> EMERGENCY_BUTTON_MODULE = item("emergency_button");
    public static final DeferredItem<Item> BUZZER_MODULE = item("buzzer");

    public static final DeferredItem<Item> KEY_ITEM = ITEMS.register("key",
            () -> new KeyItem(null, new Item.Properties().stacksTo(16)));
    public static final Map<DyeColor, DeferredItem<KeyItem>> COLORED_KEYS = itemWithColors("key", color ->
            () -> new KeyItem(color, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KeyChainItem> KEY_CHAIN = ITEMS.register("key_chain",
            () -> new KeyChainItem(new Item.Properties().stacksTo(1).component(PanelDataComponents.KEY_CHAIN_CONTENTS, KeyChainContents.EMPTY)));

    static {
        if (FMLLoader.getDist().isClient()) {;
            WikiableEntries.register(CABLE_STRIPPER.getId(),
                    WikiPage.of(CABLE_STRIPPER).category(PanelWikiCategories.TOOLS)
                            .addParagraph("Use on a dashpanels:cable to strip it. This will then make it a Stripped Cable.")
                            .addParagraph("Use on a Stripped Cable to configure it's module. To configure just scroll in the gui and click when you're on your selected module.")
                            .addParagraph("Sneak use on a dashpanels:cable or a Stripped Cable to easily pick them up.")
            );
            WikiableEntries.register(PAINT_BRUSH.getId(),
                    WikiPage.of(PAINT_BRUSH).category(PanelWikiCategories.TOOLS)
                            .addParagraph("Use on dashpanels:control_panel to edit it's skin, some skins are colorable which means you can edit the color.")
                            .addParagraph("In your instance file there's a path titled \"skin_palettes\" where you can find the skin color palettes you have saved.")
            );
            WikiableEntries.register(WRENCH.getId(),
                    WikiPage.of(WRENCH).category(PanelWikiCategories.TOOLS)
                            .addParagraph("Use on modules to configure them, configuring a module can vary from inverting the signal to controlling how many switches there are, like the dashpanels:push_button .")
                            .addParagraph("Sneak using on a module will remove it, unless it won't allow to be removed, like the dashpanels:key_switch .")
            );
            WikiableEntries.register(KEY_ITEM.getId(),
                    WikiPage.of(KEY_ITEM).category(PanelWikiCategories.SPECIAL_ITEMS)
                            .addParagraph("Use on the dashpanels:key_switch to pair the key. Then after it's paired you can insert the key for it to be turned to send out a signal.")
                            .addParagraph("This key can be copied in the crafting table by putting a single paired key with non paired keys to copy the key.")
                            .addParagraph("A paired key can be cleared by putting it by itself in the crafting table"));
            COLORED_KEYS.values().forEach(item -> WikiableEntries.registerRedirect(
                    item.getId(),
                    KEY_ITEM.getId()
            ));
            WikiableEntries.register(KEY_CHAIN.getId(),
                    WikiPage.of(KEY_CHAIN).category(PanelWikiCategories.SPECIAL_ITEMS)
                            .addParagraph("You can stack multiple of a dashpanels:key on this and use it as a sort of skeleton key.")
                            .addParagraph("You can stack this on top of a bound dashpanels:key or stack a bound dashpanels:key on this, similar to a bundle."));

            //Modules
            WikiableEntries.register(SWITCH_MODULE.getId(),
                    WikiPage.of(SWITCH_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("A simple flip switch, when right clicked it toggles. Works the same as a lever.")
                            .addParagraph("Has config options.")
            );
            WikiableEntries.register(KNOB_MODULE.getId(),
                    WikiPage.of(KNOB_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("A knob, like the name says. Use to start holding and drag left and right to turn it.")
                            .addParagraph("Has config options.")
            );
            WikiableEntries.register(CONTROL_LEVER_MODULE.getId(),
                    WikiPage.of(CONTROL_LEVER_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("The control lever allows for analog output, like the knob. Use to start holding and drag up and down to change the value.")
                            .addParagraph("Has config options.")
            );
            WikiableEntries.register(INDICATOR_BULB_MODULE.getId(),
                    WikiPage.of(INDICATOR_BULB_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("The indicator bulb glows when given power.")
                            .addParagraph("Has config options.")
            );
            WikiableEntries.register(MOMENTARY_SWITCH_MODULE.getId(),
                    WikiPage.of(MOMENTARY_SWITCH_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("The momentary switch is a more advanced button.")
                            .addParagraph("Has config options.")
            );
            WikiableEntries.register(JOYSTICK_MODULE.getId(),
                    WikiPage.of(JOYSTICK_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("The joystick gives out 4 outputs, like the control lever and knob you can use and hold, drag left right up and down to move it. After you let go it repositions to the center.")
                            .addParagraph("Has config options.")
            );
            WikiableEntries.register(LABEL_MODULE.getId(),
                    WikiPage.of(LABEL_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("Labels stuff, just change the name to change the label value.")
            );
            WikiableEntries.register(SEVEN_SEGMENT_MODULE.getId(),
                    WikiPage.of(SEVEN_SEGMENT_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("You can this module a redstone output. It will display the signal power.")
            );
            WikiableEntries.register(PUSH_BUTTON_MODULE.getId(),
                    WikiPage.of(PUSH_BUTTON_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("The push button is just a selection switch, depending on the button you press it will change to that button.")
                            .addParagraph("Has config options.")
            );
            WikiableEntries.register(KEY_SWITCH_MODULE.getId(),
                    WikiPage.of(KEY_SWITCH_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("The first pairing module. Pairs with the dashpanels:key .")
                            .addParagraph("Has config options.")
            );
            WikiableEntries.register(EMERGENCY_BUTTON_MODULE.getId(),
                    WikiPage.of(EMERGENCY_BUTTON_MODULE).category(PanelWikiCategories.MODULES)
                            .addParagraph("A toggling button with a cover")
                            .addParagraph("Has config options."));
        }
    }

    public static DeferredItem<Item> item(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    public static <T extends Item> Map<DyeColor, DeferredItem<T>> itemWithColors(String id, Function<DyeColor, Supplier<T>> factory) {
        Map<DyeColor, DeferredItem<T>> ret = Maps.newEnumMap(DyeColor.class);
        Arrays.stream(DyeColor.values()).forEach(color -> ret.put(color, ITEMS.register("%s_%s".formatted(color.getSerializedName(), id), factory.apply(color))));
        return ret;
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
