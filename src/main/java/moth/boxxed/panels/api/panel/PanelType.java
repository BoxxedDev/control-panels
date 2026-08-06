package moth.boxxed.panels.api.panel;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.util.EnumStreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public enum PanelType implements StringRepresentable {
    DEFAULT(PanelBlocks.CONTROL_PANEL.get(), PanelBlockEntities.PANEL.get(), Dashpanels.path("default")),
    WALL(PanelBlocks.WALL_CONTROL_PANEL.get(), PanelBlockEntities.WALL_PANEL.get(), Dashpanels.path("wall")),
    CEILING(PanelBlocks.CEILING_CONTROL_PANEL.get(), PanelBlockEntities.CEILING_PANEL.get(), Dashpanels.path("ceiling"));

    public final Block block;
    public final BlockEntityType blockEntity;
    public final ResourceLocation defaultSkin;

    PanelType(Block block, BlockEntityType<?> blockEntity, ResourceLocation defaultSkin) {
        this.block = block;
        this.blockEntity = blockEntity;
        this.defaultSkin = defaultSkin;
    }

    public static final EnumCodec<PanelType> CODEC = StringRepresentable.fromEnum(PanelType::values);
    public static final EnumStreamCodec<PanelType> STREAM_CODEC = new EnumStreamCodec<>(PanelType.class);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}