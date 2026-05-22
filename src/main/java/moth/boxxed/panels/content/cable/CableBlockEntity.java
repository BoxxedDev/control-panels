package moth.boxxed.panels.content.cable;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.network.connecting_panels.ConnectingModulesNetwork;
import moth.boxxed.panels.network.connecting_panels.IConnectingModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.HashMap;
import java.util.Map;

public class CableBlockEntity extends BlockEntity implements IConnectingModules {
    public ConnectingModulesNetwork network;

    public CableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelBlockEntities.CABLE.get(), pos, blockState);
    }

    @Override
    public ModuleMap getModules() {
        return ModuleMap.Empty();
    }

    @Override
    public boolean isConnecting(Direction direction) {
        return (Direction.Plane.HORIZONTAL.test(direction));
    }

    @Override
    public ConnectingModulesNetwork getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(ConnectingModulesNetwork network) {
        this.network = network;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (this.getNetwork() == null)
            this.setNetwork(ConnectingModulesNetwork.getOrMake(level, blockPos));
    }
}
