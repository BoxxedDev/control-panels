package moth.boxxed.panels.datagen;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelCreativeTabs;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class PanelLangProvider extends LanguageProvider {
    public PanelLangProvider(PackOutput output) {
        super(output, ControlPanels.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        //Module items
        addItem(PanelItems.SWITCH_MODULE, "Switch");
        addItem(PanelItems.KNOB_MODULE, "Knob");
        addItem(PanelItems.CONTROL_LEVER_MODULE, "Control Lever");

        //Non module stuff
        addBlock(PanelBlocks.CONTROL_PANEL, "Control Panel");
        addBlock(PanelBlocks.CABLE, "Control Cable");
        addBlock(PanelBlocks.STRIPPED_CABLE, "Stripped Cable");
        addItem(PanelItems.CABLE_STRIPPER, "Cable Stripper");

        addWidget("panel.exit", "Exit");
        addWidget("panel.save", "Save");
        addWidget("panel.write_name", "Write Name");
        addWidget("panel.edit_box.module_name", "Module Name");

        addTooltip("shift_to_expand", "§bHold §3[Shift] §r§bfor more info");

        addTooltip("cable_stripper_info_1", "§bRight-click §3a normal cable to strip it");
        addTooltip("cable_stripper_info_2", "§bRight-click §3a stripped cable to change \n   its configured input or output module");
        addTooltip("cable_stripper_info_3", "§bCrouch Right-click §3to pick up normal cables \n   or stripped cables");

        addCreativeTab(PanelCreativeTabs.PANEL_TAB.get(), "Control Panel");
        addCreativeTab(PanelCreativeTabs.MODULES_TAB.get(), "Control Panel Modules");
    }

    private void addWidget(String key, String string) {
        addCustom("widget", key, string);
    }

    private void addCustom(String start, String key, String string) {
        add("%s.%s.%s".formatted(start, ControlPanels.MOD_ID, key), string);
    }

    private void addTooltip(String key, String string) {
        addCustom("tooltip", key, string);
    }

    private void addCreativeTab(CreativeModeTab tab, String string) {
        add(tab.getDisplayName().getString(), string);
    }
}
