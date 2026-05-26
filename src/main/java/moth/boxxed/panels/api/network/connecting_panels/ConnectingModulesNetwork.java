package moth.boxxed.panels.api.network.connecting_panels;

import moth.boxxed.panels.content.cable.CableBlockEntity;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlockEntity;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConnectingModulesNetwork {
    public UUID id;
    public Set<BlockPos> panels;
    public Set<BlockPos> cables;
    public Set<BlockPos> outputs;
    public Level level;

    public ConnectingModulesNetwork(Level level, UUID id) {
        this.level = level;
        this.id = id;

        this.panels = new HashSet<>();
        this.cables = new HashSet<>();
        this.outputs = new HashSet<>();
    }

    public void addMembers(Set<BlockPos> posList) {
        posList.forEach(this::addMember);
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
            this.outputs.add(outputPos);
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
            this.outputs.remove(outputPos);
    }

    public boolean isMember(BlockPos pos) {
        return this.getMember(pos).isMember;
    }

    public MemberType getMember(BlockPos pos) {
        if (this.panels.contains(pos))
            return MemberType.PANEL;
        if (this.cables.contains(pos))
            return MemberType.CABLE;
        if (this.outputs.contains(pos))
            return MemberType.OUTPUT;
        return MemberType.NOT;
    }

    public Set<BlockPos> getAllMembers() {
        Set<BlockPos> collectivePositions = new HashSet<>();
        collectivePositions.addAll(this.panels);
        collectivePositions.addAll(this.cables);
        collectivePositions.addAll(this.outputs);
        return collectivePositions;
    }

    public void merge(List<ConnectingModulesNetwork> surroundingNetworks) {
        surroundingNetworks.forEach(network -> {
            this.addMembers(network.panels);
            this.addMembers(network.cables);
            this.addMembers(network.outputs);
        });
    }

    public void removeMember(BlockPos pos) {
        MemberType type = this.getMember(pos);
        if (type == MemberType.PANEL)
            this.removePanel(pos);
        if (type == MemberType.CABLE)
            this.removeCable(pos);
        if (type == MemberType.OUTPUT)
            this.removeOutput(pos);
    }

    public enum MemberType {
        PANEL(true),
        CABLE(true),
        OUTPUT(true),
        NOT(false);

        public final boolean isMember;
        MemberType(boolean isMember) {
            this.isMember = isMember;
        }
    }
}