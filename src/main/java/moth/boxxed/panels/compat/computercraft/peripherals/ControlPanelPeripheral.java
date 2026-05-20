package moth.boxxed.panels.compat.computercraft.peripherals;

import dan200.computercraft.api.peripheral.IPeripheral;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public class ControlPanelPeripheral implements IPeripheral {
    private PanelBlockEntity blockEntity;
    public ControlPanelPeripheral(BlockEntity blockEntity) {
        this.blockEntity = (PanelBlockEntity) blockEntity;
    }

    @Override
    public String getType() {
        return "control_panel";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }
}
