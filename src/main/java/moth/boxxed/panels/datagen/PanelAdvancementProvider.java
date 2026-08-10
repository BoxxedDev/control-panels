package moth.boxxed.panels.datagen;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PanelAdvancementProvider extends AdvancementProvider {
    public PanelAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(
                new MainGenerator()
        ));
    }

    private static final class MainGenerator implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            var root = Advancement.Builder.advancement()
                    .display(
                    new ItemStack(PanelBlocks.CONTROL_PANEL),
                    Component.translatable("advancements.dashpanels.root.title"),
                    Component.translatable("advancements.dashpanels.root.desc"),
                    Dashpanels.path("textures/gui/advancement.png"),
                    AdvancementType.TASK,
                    false,
                    false,
                    false)
                    .addCriterion(
                            "has_control_panel",
                            InventoryChangeTrigger.TriggerInstance.hasItems(PanelBlocks.CONTROL_PANEL.get())
                    ).save(saver, Dashpanels.path("root"), existingFileHelper);

            var firstModule = Advancement.Builder.advancement()
                    .display(
                            new ItemStack(PanelItems.CONTROL_LEVER_MODULE.get()),
                            Component.translatable("advancements.dashpanels.first_module.title"),
                            Component.translatable("advancements.dashpanels.first_module.desc"),
                            Dashpanels.path("textures/gui/advancement.png"),
                            AdvancementType.GOAL,
                            true,
                            true,
                            false)
                    .addCriterion(
                            "has_module",
                            InventoryChangeTrigger.TriggerInstance.hasItems(
                                    ModulesRegistry.MODULE_REGISTRY.entrySet().stream()
                                            .map(Map.Entry::getValue)
                                            .map(ModuleType::getItemFromType)
                                            .toArray(Item[]::new)
                            ))
                    .parent(root)
                    .save(saver, Dashpanels.path("first_module"), existingFileHelper);

            var cableStripper = Advancement.Builder.advancement()
                    .display(
                            new ItemStack(PanelItems.CABLE_STRIPPER.get()),
                            Component.translatable("advancements.dashpanels.cable_stripper.title"),
                            Component.translatable("advancements.dashpanels.cable_stripper.desc"),
                            Dashpanels.path("textures/gui/advancement.png"),
                            AdvancementType.GOAL,
                            true,
                            false,
                            false)
                    .addCriterion(
                            "has_cable_stripper",
                            InventoryChangeTrigger.TriggerInstance.hasItems(PanelItems.CABLE_STRIPPER))
                    .parent(root)
                    .save(saver, Dashpanels.path("cable_stripper"), existingFileHelper);

            var wrench = Advancement.Builder.advancement()
                    .display(
                            new ItemStack(PanelItems.WRENCH.get()),
                            Component.translatable("advancements.dashpanels.wrench.title"),
                            Component.translatable("advancements.dashpanels.wrench.desc"),
                            Dashpanels.path("textures/gui/advancement.png"),
                            AdvancementType.GOAL,
                            true,
                            false,
                            false)
                    .addCriterion(
                            "has_wrench",
                            InventoryChangeTrigger.TriggerInstance.hasItems(PanelItems.WRENCH))
                    .parent(root)
                    .save(saver, Dashpanels.path("wrench"), existingFileHelper);

            var brush = Advancement.Builder.advancement()
                    .display(
                            new ItemStack(PanelItems.PAINT_BRUSH.get()),
                            Component.translatable("advancements.dashpanels.brush.title"),
                            Component.translatable("advancements.dashpanels.brush.desc"),
                            Dashpanels.path("textures/gui/advancement.png"),
                            AdvancementType.GOAL,
                            true,
                            false,
                            false)
                    .addCriterion(
                            "has_brush",
                            InventoryChangeTrigger.TriggerInstance.hasItems(PanelItems.WRENCH))
                    .parent(root)
                    .save(saver, Dashpanels.path("brush"), existingFileHelper);
        }
    }
}
