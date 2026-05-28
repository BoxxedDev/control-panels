package moth.boxxed.panels.api.network.connecting_panels;

import moth.boxxed.panels.ControlPanels;
import net.minecraft.world.level.Level;

import java.util.*;

public class ModulesNetwork {
    public UUID id;
    public Set<ModulesNetworkMember> members;

    public ModulesNetwork() {
        this.members = new HashSet<>();
    }

    public void addMembers(Set<ModulesNetworkMember> members) {
        this.members.forEach(this::addMember);
    }
    public void addMember(ModulesNetworkMember member) {
        if (member.getLevel().isClientSide)
            return;
        if (!member.getLevel().isLoaded(member.getBlockPos()))
            return;
        if (this.members.contains(member))
            return;
        ControlPanels.LOGGER.debug("Added member {} to network {}", member, this.id);
        this.members.add(member);
        member.networkUpdate(this);
    }

    public void removeMembers(Set<ModulesNetworkMember> members) {
        members.forEach(this::removeMember);
    }
    public void removeMember(ModulesNetworkMember member) {
        if (member.getLevel().isClientSide)
            return;
        this.members.remove(member);
        ControlPanels.LOGGER.debug("Removed member {} from network {}", member, this.id);
        if (this.members.isEmpty()) {
            ModulesNetworkManager.removeNetwork(member.getLevel(), this);
            return;
        }
        Set<ModulesNetworkMember> neighbors = ModulesNetworkManager.getNeighbors(member);
        if (neighbors.size() > 1)
            this.splitNetwork(member, neighbors);
    }

    public boolean hasMember(ModulesNetworkMember member) {
        return this.members.contains(member);
    }

    private void splitNetwork(ModulesNetworkMember sourceMember, Set<ModulesNetworkMember> members) {
        Level level = sourceMember.getLevel();
        Set<ModulesNetwork> filteredNetworks = new HashSet<>();
        for (ModulesNetworkMember member : members) {
            Queue<ModulesNetworkMember> queue = new ArrayDeque<>();
            ModulesNetwork network = new ModulesNetwork();
            network.id = UUID.randomUUID();

            queue.add(member);
            while (!queue.isEmpty()) {
                ModulesNetworkMember next = queue.poll();
                network.addMember(next);
                for (ModulesNetworkMember nextMember : ModulesNetworkManager.getNeighbors(next)) {
                    if (!this.members.contains(nextMember))
                        continue;
                    if (network.hasMember(nextMember))
                        continue;
                    if (nextMember == sourceMember)
                        continue;
                    queue.add(nextMember);
                }
            }

            if (ModulesNetworkManager.hasNetwork(level, network))
                continue;
            filteredNetworks.add(network);
        }

        Map<UUID, ModulesNetwork> map = ModulesNetworkManager.ALL.computeIfAbsent(level, $ -> new HashMap<>());
        map.remove(this.id);
        for (ModulesNetwork network : filteredNetworks) {
            network.syncMemberIds();
            map.put(network.id, network);
        }
        ControlPanels.LOGGER.debug("Split network {}", this.id);
    }

    public void syncMemberIds() {
        for (ModulesNetworkMember member : this.members) {
            member.network = this.id;
        }
    }

    public void merge(Set<ModulesNetwork> networks) {
        ControlPanels.LOGGER.debug("Merging networks {} onto {}", networks, this.id);
        networks.forEach(network -> {
            if (network == null)
                return;
            this.addMembers(network.members);
            network.removeMembers(network.members);
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModulesNetwork otherNetwork)
            return this.members.containsAll(otherNetwork.members);
        return false;
    }
}