package moth.boxxed.panels.datagen;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.compat.sable.PanelSableRegistries;
import moth.boxxed.panels.config.ClientConfig;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelCreativeTabs;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class PanelLangProvider extends LanguageProvider {
    public PanelLangProvider(PackOutput output) {
        super(output, Dashpanels.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        //Module items
        addItem(PanelItems.SWITCH_MODULE, "Switch");
        addItem(PanelItems.KNOB_MODULE, "Knob");
        addItem(PanelItems.CONTROL_LEVER_MODULE, "Control Lever");
        addItem(PanelItems.INDICATOR_BULB_MODULE, "Indicator Bulb");
        addItem(PanelItems.MOMENTARY_SWITCH_MODULE, "Momentary Switch");
        addItem(PanelItems.JOYSTICK_MODULE, "Joystick");
        addItem(PanelItems.LABEL_MODULE, "Label");
        addItem(PanelItems.SEVEN_SEGMENT_MODULE, "Seven Segment");
        if (ModList.get().isLoaded("sable"))
            addItem(PanelSableRegistries.NAVBALL_MODULE, "Navball");

        //Non moduleName stuff
        addBlock(PanelBlocks.CONTROL_PANEL, "Control Panel");
        addBlock(PanelBlocks.WALL_CONTROL_PANEL, "Wall Control Panel");
        addBlock(PanelBlocks.CEILING_CONTROL_PANEL, "Ceiling Control Panel");

        addBlock(PanelBlocks.CABLE, "Control Cable");
        addBlock(PanelBlocks.STRIPPED_CABLE, "Stripped Cable");

        addItem(PanelItems.CABLE_STRIPPER, "Cable Stripper");
        addItem(PanelItems.PAINT_BRUSH, "Paint Brush");
        addItem(PanelItems.WRENCH, "Panel Wrench");

        if (ModList.get().isLoaded("create"))
            addBlock(PanelCreateRegistries.PANEL_LINK, "Panel Link");

        addWidget("panel.exit", "Exit");
        addWidget("panel.save", "Save");
        addWidget("panel.write_name", "Write Name");
        addWidget("panel.edit_box.module_name", "Module Name");
        addWidget("stripped_cable.scrollbar", "Scroll Bar");
        addWidget("panel_link.module_select", "Scroll to select moduleName");

        addTooltip("shift_to_expand", "§3Hold §b[Shift] §r§3for more info");

        addTooltip("cable_stripper_info_1", "§bRight-click §3a normal cable to strip it");
        addTooltip("cable_stripper_info_2", "§bRight-click §3a stripped cable to change its configured input or output moduleName");
        addTooltip("cable_stripper_info_3", "§bSneak Right-click §3to pick up normal cables or stripped cables");

        addCreativeTab(PanelCreativeTabs.PANEL_TAB.get(), "Dashpanels");
        add("creativetab.dashpanels.dashpanels", "§lDashpanels");
        add("creativetab.dashpanels.modules", "§lModules");

        addConfig(ClientConfig.SHOW_MODULE_TOOLTIPS, "Show Module Tooltips");
        addConfig(ClientConfig.CLICK_FOR_MODULE_HOLD, "Click for Module Hold");

        addCustom("key", "delete_module", "Delete Module");
        addCustom("key", "select_module", "Select Module");
        addCustom("key", "move_module", "Move Module");
        addCustom("key", "hold_move_camera", "Move Camera While Holding");
        add("key.categories.dashpanels", "Dashpanels");

        addModuleConfig("name", "Name");
        addModuleConfig("output", "Output");
        addModuleConfig("inverted", "Inverted");

        add("dashpanels.paint_wheel.apply", "Apply");
        add("dashpanels.paint_wheel.apply_all", "Apply To All");
    }

    private <T extends ModConfigSpec.ConfigValue<?>> void addConfig(T configValue, String string) {
        String nameKey = Dashpanels.MOD_ID + ".configuration";
        for (String path : configValue.getPath()) {
            nameKey = nameKey.concat("." + path);
        }
        add(nameKey, string);
    }

    private void addWidget(String key, String string) {
        addCustom("widget", key, string);
    }

    private void addCustom(String start, String key, String string) {
        add("%s.%s.%s".formatted(start, Dashpanels.MOD_ID, key), string);
    }

    private void addTooltip(String key, String string) {
        addCustom("tooltip", key, string);
    }

    private void addCreativeTab(CreativeModeTab tab, String string) {
        add(tab.getDisplayName().getString(), string);
    }

    private void addModuleConfig(String valueName, String string) {
        add("module_config.value.%s".formatted(valueName), string);
    }
}
