package moth.boxxed.panels.compat.create;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.compat.PanelCompat;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlock;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlockEntity;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.index.PanelCreativeTabs;
import moth.boxxed.panels.index.PanelItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.function.Supplier;

public class PanelCreateRegistries implements PanelCompat {
    public static DeferredBlock<PanelLinkBlock> PANEL_LINK;
    public static Supplier<BlockEntityType<PanelLinkBlockEntity>> PANEL_LINK_BE;

    @Override
    public void init() {
        PANEL_LINK =
                PanelBlocks.BLOCKS.register(
                        "panel_link",
                        () -> new PanelLinkBlock(
                                BlockBehaviour.Properties.of()
                                        .noOcclusion()
                        )
                );
        PanelItems.blockItem("panel_link", PANEL_LINK);
        PANEL_LINK_BE =
                PanelBlockEntities.BLOCK_ENTITY_TYPES.register(
                        "panel_link",
                        () -> BlockEntityType.Builder.of(PanelLinkBlockEntity::new, PANEL_LINK.get())
                                .build(null));
        PanelCreativeTabs.addContentTo(PanelCreativeTabs.PANEL_TAB, PANEL_LINK);
        ControlPanels.LOGGER.debug("Create Init");
    }

    @Override
    public String id() {
        return "create";
    }
}
