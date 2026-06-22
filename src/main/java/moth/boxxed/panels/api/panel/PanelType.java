package moth.boxxed.panels.api.panel;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.util.EnumStreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

public enum PanelType implements StringRepresentable {
    DEFAULT(PanelBlocks.CONTROL_PANEL.get(), Dashpanels.path("default")),
    WALL(null, Dashpanels.path("wall")),
    CEILING(null, Dashpanels.path("ceiling"));

    public final Block block;
    public final ResourceLocation defaultSkin;

    PanelType(Block block, ResourceLocation defaultSkin) {
        this.block = block;
        this.defaultSkin = defaultSkin;
    }

    public static final EnumCodec<PanelType> CODEC = StringRepresentable.fromEnum(PanelType::values);
    public static final EnumStreamCodec<PanelType> STREAM_CODEC = new EnumStreamCodec<>(PanelType.class);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}