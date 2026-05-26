package moth.boxxed.panels.index;

import moth.boxxed.panels.ControlPanels;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class PanelTags {
    //Only reason this is here is for searching reasons, as well potentially other checks
    public static class Items {
        public static final TagKey<net.minecraft.world.item.Item> MODULE = TagKey.create(Registries.ITEM, ControlPanels.path("modules"));
    }

    public static void init() {}
}
