package moth.boxxed.panels.api.network.connecting_panels;

import moth.boxxed.panels.content.cable.CableBlockEntity;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlockEntity;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.UUID;

public class ConnectingModulesNetwork {
    public UUID id;
    public Set<BlockPos> panels;
    public Set<BlockPos> cables;
    public Set<BlockPos> output;
    public Level level;

    public ConnectingModulesNetwork(Level level, UUID id) {
        this.level = level;
        this.id = id;
    }

    public void addMember(BlockPos pos) {
        this.addPanel(pos);
        this.addCable(pos);
        this.addOutput(pos);
    }

    public void addPanel(BlockPos panelPos) {
        if (this.level.getBlockEntity(panelPos) instanceof PanelBlockEntity)
            this.panels.add(panelPos);
    }
    public void addCable(BlockPos cablePos) {
        if (this.level.getBlockEntity(cablePos) instanceof CableBlockEntity)
            this.cables.add(cablePos);
    }
    public void addOutput(BlockPos outputPos) {
        if (this.level.getBlockEntity(outputPos) instanceof StrippedCableBlockEntity)
            this.output.add(outputPos);
    }

    public void removePanel(BlockPos panelPos) {
        if (this.level.getBlockEntity(panelPos) instanceof PanelBlockEntity)
            this.panels.remove(panelPos);
    }
    public void removeCable(BlockPos cablePos) {
        if (this.level.getBlockEntity(cablePos) instanceof CableBlockEntity)
            this.cables.remove(cablePos);
    }
    public void removeOutput(BlockPos outputPos) {
        if (this.level.getBlockEntity(outputPos) instanceof CableBlockEntity)
            this.output.remove(outputPos);
    }

    public boolean isMember(BlockPos pos) {
        return this.panels.contains(pos) || this.cables.contains(pos) || this.output.contains(pos);
    }
}