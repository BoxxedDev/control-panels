package moth.boxxed.panels.network.connecting_panels;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.*;

public class ConnectingModulesNetwork {
    public static final List<ConnectingModulesNetwork> ALL = new ArrayList<>();

    private Map<String, Module> collectiveModules;
    private Set<BlockPos> members;

    public ConnectingModulesNetwork() {
        ALL.add(this);
        this.collectiveModules = new HashMap<>();
        this.members = new HashSet<>();
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

    public void removeMember(Level level, BlockPos pos) {
        BlockEntity splitBE = level.getBlockEntity(pos);
        if (!(splitBE instanceof IConnectingModules connectingModulesBE)) return;
        this.members.remove(pos);
        ControlPanels.LOGGER.info("Removed member of network \"%s\" at %s".formatted(this, pos));
        List<BlockPos> connectedPositions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (!connectingModulesBE.isConnecting(direction) || !(level.getBlockEntity(pos.relative(direction)) instanceof IConnectingModules)) continue;
            connectedPositions.add(pos.relative(direction));
        }
        if (connectedPositions.size() >= 2)
            this.splitNetwork(level, pos, connectedPositions);
        if (this.members.isEmpty())
            ALL.remove(this);
    }

    public void mergeNetworks(Collection<ConnectingModulesNetwork> others) {
        ControlPanels.LOGGER.info("Merging networks \"%s\" onto \"%s\"".formatted(others.toString(), this));
        for (ConnectingModulesNetwork other : others) {
            this.members.addAll(other.getMembers());
            ALL.remove(other);
        }
    }

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
                    if (!connectingModulesBE.isConnecting(direction)) continue;
                    if (!(level.getBlockEntity(nextPos.relative(direction)) instanceof IConnectingModules)) continue;
                    queue.add(nextPos.relative(direction));
                }
            }
            if (foundList.isEmpty() || setsToAdd.contains(foundList)) continue;
            setsToAdd.add(foundList);
        }
        for (Set<BlockPos> set : setsToAdd) {
            ConnectingModulesNetwork newNetwork = new ConnectingModulesNetwork();
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
            ConnectingModulesNetwork ret = new ConnectingModulesNetwork();
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
}