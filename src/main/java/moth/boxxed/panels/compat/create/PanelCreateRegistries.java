package moth.boxxed.panels.compat.create;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import com.simibubi.create.api.schematic.requirement.SchematicRequirementRegistries;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.api.wiki.WikiPage;
import moth.boxxed.panels.api.wiki.WikiableEntries;
import moth.boxxed.panels.compat.PanelCompat;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlock;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlockEntity;
import moth.boxxed.panels.compat.create.panel_link.screen.PanelLinkMenu;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlockEntity;
import moth.boxxed.panels.index.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

public class PanelCreateRegistries implements PanelCompat {
    private static DeferredRegister<DisplaySource> DISPLAY_SOURCES;

    public static DeferredBlock<PanelLinkBlock> PANEL_LINK;
    public static Supplier<BlockEntityType<PanelLinkBlockEntity>> PANEL_LINK_BE;
    public static Supplier<MenuType<PanelLinkMenu>> PANEL_LINK_MENU;
    public static Supplier<DisplaySource> PANEL_DISPLAY_SOURCE;

    @Override
    public void init() {
        DISPLAY_SOURCES = DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, Dashpanels.MOD_ID);

        PANEL_LINK = PanelBlocks.BLOCKS.register(
                        "panel_link",
                        () -> new PanelLinkBlock(
                                BlockBehaviour.Properties.of()
                                        .noOcclusion()
                                        .destroyTime(1f)
                                        .sound(SoundType.WOOD)
                                        .mapColor(MapColor.WOOD)
                        )
                );
        PanelItems.blockItem("panel_link", PANEL_LINK);

        if (FMLLoader.getDist().isClient()) {
            WikiableEntries.register(PANEL_LINK.getId(),
                    WikiPage.of(PANEL_LINK)
                            .category(PanelWikiCategories.BLOCKS)
                            .addParagraph("The panel link is an explicit compat block. If create is not installed this block is not there.")
                            .addParagraph("If you use with an empty hand it will open a menu to configure the entries. Which correspond to a module in the connected network to be used for redstone links.")
            );
        }

        PANEL_LINK_BE = PanelBlockEntities.BLOCK_ENTITY_TYPES.register(
                        "panel_link",
                        () -> BlockEntityType.Builder.of(PanelLinkBlockEntity::new, PANEL_LINK.get())
                                .build(null)
                );
//        PanelCreativeTabs.addContentTo(PanelCreativeTabs.PANEL_TAB, PANEL_LINK);

        PANEL_LINK_MENU = PanelMenuTypes.MENU_TYPES.register(
                "panel_link", () -> IMenuTypeExtension.create(PanelLinkMenu::new)
        );

//        PANEL_DISPLAY_SOURCE = DISPLAY_SOURCES.register("panel", PanelDisplaySource::new);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (PanelType panelType : PanelType.values()) {
                SafeNbtWriterRegistry.REGISTRY.register(panelType.blockEntity, (be, tag, registries) -> {
                    if (be instanceof AbstractPanelBlockEntity pbe) {
                        pbe.saveExternal(tag, registries);
                    }
                });

                SchematicRequirementRegistries.BLOCK_ENTITIES.register(panelType.blockEntity, (be, state) -> {
                    ItemRequirement ret = ItemRequirement.NONE;
                    if (be instanceof AbstractPanelBlockEntity pbe) {
                        for (Module module : pbe.getModules().values()) {
                            if (ModuleType.getItemFromType(module.type) != null) {
                                ret = ret.union(new ItemRequirement(
                                        ItemRequirement.ItemUseType.CONSUME,
                                        new ItemStack(ModuleType.getItemFromType(module.type))
                                ));
                            }
                        }
                    }
                    return ret;
                });
            }

            SafeNbtWriterRegistry.REGISTRY.register(PANEL_LINK_BE.get(), (be, tag, registries) -> {
                if (be instanceof PanelLinkBlockEntity plbe) {
                    plbe.saveExternal(tag, registries);
                }
            });

            SafeNbtWriterRegistry.REGISTRY.register(PanelBlockEntities.STRIPPED_CABLE.get(), (be, tag, registries) -> {
                if (be instanceof StrippedCableBlockEntity scbe) {
                    scbe.saveExternal(tag, registries);
                }
            });
        });
    }

    @Override
    public void busInit(IEventBus bus) {
        bus.addListener(this::commonSetup);
//        DISPLAY_SOURCES.register(bus);
    }

    @Override
    public void postInit() {
//        DisplaySource.BY_BLOCK.add(PanelBlocks.CONTROL_PANEL.get(), PANEL_DISPLAY_SOURCE.get());
    }

    @Override
    public String id() {
        return "create";
    }
}
