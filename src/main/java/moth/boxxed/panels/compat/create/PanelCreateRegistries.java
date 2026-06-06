package moth.boxxed.panels.compat.create;

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
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.function.Supplier;

public class PanelCreateRegistries implements PanelCompat {
    public static DeferredBlock<PanelLinkBlock> PANEL_LINK;
    public static Supplier<BlockEntityType<PanelLinkBlockEntity>> PANEL_LINK_BE;
    public static Supplier<MenuType<PanelLinkMenu>> PANEL_LINK_MENU;

    @Override
    public void init() {
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
        PANEL_LINK_BE = PanelBlockEntities.BLOCK_ENTITY_TYPES.register(
                        "panel_link",
                        () -> BlockEntityType.Builder.of(PanelLinkBlockEntity::new, PANEL_LINK.get())
                                .build(null)
                );
        PanelCreativeTabs.addContentTo(PanelCreativeTabs.PANEL_TAB, PANEL_LINK);

        PANEL_LINK_MENU = PanelMenuTypes.MENU_TYPES.register(
                "panel_link", () -> IMenuTypeExtension.create(PanelLinkMenu::new)
        );
    }

    @Override
    public String id() {
        return "create";
    }
}
