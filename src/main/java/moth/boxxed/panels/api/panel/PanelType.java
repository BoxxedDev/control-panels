package moth.boxxed.panels.api.panel;

import moth.boxxed.panels.util.EnumStreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PanelType implements StringRepresentable {
    DEFAULT,
    WALL,
    CEILING,
    FLOOR;

    public static final EnumCodec<PanelType> CODEC = StringRepresentable.fromEnum(PanelType::values);
    public static final EnumStreamCodec<PanelType> STREAM_CODEC = new EnumStreamCodec<>(PanelType.class);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
