package moth.boxxed.panels.datagen;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class PanelModelProviders {
    public static class Item extends ItemModelProvider {
        public Item(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Dashpanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            withExistingParent("control_panel", Dashpanels.path("block/control_panel/single"));
            withExistingParent("wall_control_panel", Dashpanels.path("block/wall_control_panel/single"));
            withExistingParent("ceiling_control_panel", Dashpanels.path("block/ceiling_control_panel/single"));

            withExistingParent("panel_link", Dashpanels.path("block/control_link"));

            toolItem(PanelItems.CABLE_STRIPPER.get());
            toolItem(PanelItems.PAINT_BRUSH.get());
            toolItem(PanelItems.WRENCH.get());
            toolItem(PanelItems.KEY_ITEM.get());
            PanelItems.COLORED_KEYS.forEach((color ,item) -> {
                ResourceLocation location = Dashpanels.path("color_keys/" + color.getSerializedName() + "_key");
                toolItem(item.get().toString(),location);
            });
            basicItem(Dashpanels.path("key_outline"));

            toolItem(PanelItems.KEY_CHAIN.get());
        }

        public ItemModelBuilder toolItem(net.minecraft.world.item.Item item) {
            ResourceLocation itemKey = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
            return toolItem(item.toString(), itemKey);
        }

        public ItemModelBuilder toolItem(String key, ResourceLocation location) {
            return getBuilder(key)
                    .parent(new ModelFile.UncheckedModelFile(Dashpanels.path("item/tool")))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "item/" + location.getPath()));
        }
    }

    public static class Block extends BlockModelProvider {
        public Block(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Dashpanels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
        }
    }
}
