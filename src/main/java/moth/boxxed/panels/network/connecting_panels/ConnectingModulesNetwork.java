package moth.boxxed.panels.network.connecting_panels;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.*;

//TODO: eventually refactor everything to do with the network so it's not so fucked up
public class ConnectingModulesNetwork {
    public static final List<ConnectingModulesNetwork> ALL = new ArrayList<>();

    private final ModuleMap collectiveModules;
    private final Set<BlockPos> members;
    private final Level level;

    public ConnectingModulesNetwork(Level level) {
        ALL.add(this);
        this.collectiveModules = new ModuleMap();
        this.members = new HashSet<>();
        this.level = level;
    }

    public String generateNewName(ModuleType<?> moduleType) {
        String baseName = ModulesRegistry.MODULE_REGISTRY.getKey(moduleType).getPath()+"_%d";
        int i=0;
        while (this.collectiveModules.containsKey(baseName.formatted(i)))
            i++;
        return baseName.formatted(i);
    }

    public boolean validateName(String name) {
        return !this.collectiveModules.containsKey(name);
    }

    public Set<BlockPos> getMembers() {
        return this.members;
    }

    public void addMember(BlockPos pos) {
        this.members.add(pos);
        ControlPanels.LOGGER.info("Added member to network \"%s\" at %s".formatted(this, pos.toShortString()));
    }

    public void addAllMembers(Collection<BlockPos> collection) {
        this.members.addAll(collection);
        ControlPanels.LOGGER.info("Added members of \"%s\" to network \"%s\"".formatted(collection, this));
    }

    public void removeMember(BlockPos pos) {
        BlockEntity splitBE = this.level.getBlockEntity(pos);
        if (!(splitBE instanceof IConnectingModules connectingModulesBE)) return;
        this.removeAllModules(connectingModulesBE.getModules());
        this.members.remove(pos);
        ControlPanels.LOGGER.info("Removed member of network \"%s\" at %s".formatted(this, pos));
        List<BlockPos> connectedPositions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (!(this.level.getBlockEntity(pos.relative(direction)) instanceof IConnectingModules other)) continue;
            if (!(connectingModulesBE.isConnecting(direction) && other.isConnecting(direction))) continue;
            connectedPositions.add(pos.relative(direction));
        }
        if (connectedPositions.size() >= 2)
            this.splitNetwork(level, pos, connectedPositions);
        if (this.members.isEmpty())
            ALL.remove(this);
    }

    public boolean hasMember(BlockPos relative) {
        return this.members.contains(relative);
    }

    public void mergeNetworks(Collection<ConnectingModulesNetwork> others) {
        ControlPanels.LOGGER.info("Merging networks \"%s\" onto \"%s\"".formatted(others.toString(), this));
        for (ConnectingModulesNetwork other : others) {
            this.members.addAll(other.getMembers());
            ALL.remove(other);
        }
    }

    //TODO: Make this code less evil
    public void splitNetwork(Level level, BlockPos splitPos, Collection<BlockPos> startingPoints) {
        BlockEntity splitBE = level.getBlockEntity(splitPos);
        if (!(splitBE instanceof IConnectingModules)) return;
        ControlPanels.LOGGER.info("Splitting network \"%s\" at %s".formatted(this, splitPos.toShortString()));
        List<Set<BlockPos>> setsToAdd = new ArrayList<>();
        for (BlockPos startingPoint : startingPoints) {
            Set<BlockPos> foundList = new HashSet<>();
            Queue<BlockPos> queue = new ArrayDeque<>();
            queue.add(startingPoint);
            while (!queue.isEmpty()) {
                BlockPos nextPos = queue.poll();
                BlockEntity be = level.getBlockEntity(nextPos);
                if (!(be instanceof IConnectingModules connectingModulesBE)) continue;
                foundList.add(nextPos);
                for (Direction direction : Direction.values()) {
                    if (nextPos.relative(direction).equals(splitPos)) continue;
                    if (this.members.contains(nextPos.relative(direction))) continue;
                    if (foundList.contains(nextPos.relative(direction))) continue;
                    if (!(level.getBlockEntity(nextPos.relative(direction)) instanceof IConnectingModules other)) continue;
                    if (!(connectingModulesBE.isConnecting(direction) && other.isConnecting(direction))) continue;
                    queue.add(nextPos.relative(direction));
                }
            }
            if (foundList.isEmpty() || setsToAdd.contains(foundList)) continue;
            setsToAdd.add(foundList);
        }
        for (Set<BlockPos> set : setsToAdd) {
            ConnectingModulesNetwork newNetwork = new ConnectingModulesNetwork(level);
            newNetwork.addAllMembers(set);
        }
    }

    @Nullable
    public static ConnectingModulesNetwork getOrMake(Level level, BlockPos pos) {
        if (level.isClientSide()) return null;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IConnectingModules connectingModulesBE) {
            List<ConnectingModulesNetwork> networks = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                if (!connectingModulesBE.isConnecting(direction)) continue;

                BlockPos otherPos = pos.relative(direction);
                if (level.getBlockEntity(otherPos) instanceof IConnectingModules other
                        && !networks.contains(other.getNetwork())
                        && other.getNetwork() != null)
                    networks.add(other.getNetwork());
            }
            networks = networks.stream().filter((network) -> network!=null).toList();
            ConnectingModulesNetwork ret = new ConnectingModulesNetwork(level);
            if (networks.size() > 1) {
                ret.mergeNetworks(networks);
            } else if (networks.size()==1) {
                networks.getFirst().addMember(pos);
                return networks.getFirst();
            }
            ret.addMember(pos);
            return ret;
        }

        return null;
    }

    public Level getLevel() {
        return this.level;
    }

    public void removeAllModules(ModuleMap modules) {
        for (Map.Entry<String, Module> entry : modules) {
            this.removeModule(entry.getKey(), entry.getValue());
        }
    }

    public void removeModule(String name, Module module) {
        this.collectiveModules.remove(name, module);
    }

    public void addAllModules(ModuleMap modules) {
        this.collectiveModules.putAll(modules);
    }

    public void addModule(String name, Module module) {
        this.collectiveModules.put(name, module);
    }

    public ModuleMap getCollectiveModules() {
        return this.collectiveModules;
    }
}