package moth.boxxed.panels.compat.create;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.wiki.WikiPage;
import moth.boxxed.panels.api.wiki.WikiableEntries;
import moth.boxxed.panels.compat.PanelCompat;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlock;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlockEntity;
import moth.boxxed.panels.compat.create.panel_link.screen.PanelLinkMenu;
import moth.boxxed.panels.index.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    @Override
    public void busInit(IEventBus bus) {
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
