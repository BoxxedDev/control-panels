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
        addItem(PanelItems.SWITCH_MODULE, "Switch");
        addItem(PanelItems.KNOB_MODULE, "Knob");
        addItem(PanelItems.CONTROL_LEVER_MODULE, "Control Lever");

        addBlock(PanelBlocks.CONTROL_PANEL, "Control Panel");
        addBlock(PanelBlocks.CABLE, "Control Cable");
        addBlock(PanelBlocks.STRIPPED_CABLE, "Stripped Cable");

        addWidget("panel.exit", "Exit");
        addWidget("panel.save", "Save");
        addWidget("panel.write_name", "Write Name");
        addWidget("panel.edit_box.module_name", "Module Name");

        addCreativeTab(PanelCreativeTabs.PANEL_TAB.get(), "Control Panel");
        addCreativeTab(PanelCreativeTabs.MODULES_TAB.get(), "Control Panel Modules");
    }

    private void addWidget(String key, String string) {
        add("widget.%s.%s".formatted(ControlPanels.MOD_ID, key), string);
    }

    private void addCreativeTab(CreativeModeTab tab, String string) {
        add(tab.getDisplayName().getString(), string);
    }
}
