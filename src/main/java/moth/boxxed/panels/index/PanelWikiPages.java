package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.wiki.ItemlessWikiPage;
import moth.boxxed.panels.api.wiki.WikiableEntries;

public class PanelWikiPages {
    public static final ItemlessWikiPage MODULES = WikiableEntries.register(Dashpanels.path("page/modules"),
            ItemlessWikiPage.of("dashpanels.wiki.page.modules").category(PanelWikiCategories.MODULES)
                    .setPriority(1000)
                    .addParagraph("All modules")
                    .addParagraph("• dashpanels:switch")
                    .addParagraph("• dashpanels:knob")
                    .addParagraph("• dashpanels:control_lever")
                    .addParagraph("• dashpanels:indicator_bulb")
                    .addParagraph("• dashpanels:momentary_switch")
                    .addParagraph("• dashpanels:joystick")
                    .addParagraph("• dashpanels:label")
                    .addParagraph("• dashpanels:seven_segment")
                    .addParagraph("• dashpanels:push_button")
                    .addParagraph("• dashpanels:key_switch")
    );

    public static void init() {}
}
