package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PanelTags {
    //Only reason this is here is for searching reasons, as well potentially other checks
    public static class Items {
        public static final TagKey<net.minecraft.world.item.Item> MODULE = TagKey.create(Registries.ITEM, Dashpanels.path("modules"));
        public static final TagKey<Item> PANELS = TagKey.create(Registries.ITEM, Dashpanels.path("panels"));
        public static final TagKey<Item> WRENCH = TagKey.create(Registries.ITEM, Dashpanels.path("config_wrench"));
    }

    public static class Blocks {
        public static final TagKey<Block> PANELS = TagKey.create(Registries.BLOCK, Dashpanels.path("panels"));
    }

    public static void init() {}
}
