package moth.boxxed.panels.api.network;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class ModulesNetwork {
    public final UUID id;
    public final Level level;
    private final Set<ModulesNetworkMember> members = Collections.newSetFromMap(new WeakHashMap<>());
    private ModuleMap compiledModules = null;

    public ModulesNetwork(Level level, UUID id){
        this.id = id;
        this.level = level;
    }



    public void addMembers(Set<ModulesNetworkMember> members) {
        this.members.forEach(this::addMember);
    }
    public void addMember(ModulesNetworkMember member) {
        if (this.members.add(member)) this.dirty();
    }

    public void removeMembers(Set<ModulesNetworkMember> members, boolean split) {
        members.forEach(x -> this.removeMember(x, split));
    }
    public void removeMember(ModulesNetworkMember member, boolean split) {
        if(!this.members.remove(member)) return;
        this.dirty();

        // if this is called, we can assume neighboring blocks are loaded anyway
        if (this.members.isEmpty()) {
            ModulesNetworkManager.removeNetwork(this);
        } else if(split) {
            Set<ModulesNetworkMember> neighbors = ModulesNetworkManager.getNeighbors(member);
            if (neighbors.size() > 1) this.splitNetwork(member, neighbors);
        }
    }

    public boolean hasMember(ModulesNetworkMember member) {
        return this.members.contains(member);
    }

    private void splitNetwork(ModulesNetworkMember source, Set<ModulesNetworkMember> members) {
        Set<ModulesNetworkMember> traversed = new HashSet<>();
        Map<ModulesNetworkMember,Set<ModulesNetworkMember>> partitions = new HashMap<>();
        Queue<ModulesNetworkMember> queue = new ArrayDeque<>();

        traversed.add(source);
        for (ModulesNetworkMember member : members) {
            Set<ModulesNetworkMember> component = new HashSet<>();
            partitions.put(member, component);
            queue.add(member);

            while (!queue.isEmpty()) {
                ModulesNetworkMember next = queue.poll();
                if (next == null || !traversed.add(next)) continue;
                component.add(next);
                ModulesNetworkManager.getNeighbors(next).forEach(queue::add);
            }
        }

        boolean first = true;
        for (Set<ModulesNetworkMember> component : partitions.values()) {
            if(component.isEmpty()) continue;
            if (first) first = false;
            else {
                UUID uuid = UUID.randomUUID();
                component.forEach(x -> x.setNetwork(uuid));
            }
        }
    }

    public void merge(Set<ModulesNetwork> networks) {
        for (ModulesNetwork network : networks) {
            new HashSet<>(network.members).forEach(x -> x.setNetwork(this.id));
        }
    }

    public void dirty() {
        this.compiledModules = null;
    }
    public ModuleMap getCompiledModules() {
        if(this.compiledModules == null) {
            ModuleMap collective = new ModuleMap();
            for (ModulesNetworkMember member : this.members) {
                if (member.getModules() == null) continue;
                collective.putAll(new HashMap<>(member.getModules()));
            }
            this.compiledModules = collective;
        }
        return this.compiledModules;
    }

    public boolean hasModule(String module) {
        return this.compiledModules.containsKey(module);
    }

    public String validateName(String name, Module.ModuleInfo module) {
        ModuleMap collective = getCompiledModules();
        if (collective.containsKey(name) || name == null) {
            String location = module.type().getPath();
            String ret = location+"_0";
            int i=0;
            while (collective.containsKey(ret)) {
                i++;
                ret = (location+"_%d").formatted(i);
            }
            return ret;
        }
        return name;
    }
}