package moth.boxxed.panels.api.network.connecting_panels;

import moth.boxxed.panels.ControlPanels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.*;

public class ModulesNetworkManager {
    public static final Map<LevelAccessor, Map<UUID, ModulesNetwork>> ALL = new HashMap<>();

    @SubscribeEvent
    public static void onLoad(LevelEvent.Load event) {
        ALL.put(event.getLevel(), new HashMap<>());
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        ALL.remove(event.getLevel());
    }

    public static ModulesNetwork getNetwork(ModulesNetworkMember member) {
        UUID id = member.network;
        Map<UUID, ModulesNetwork> map = ALL.computeIfAbsent(member.getLevel(), $ -> new HashMap<>());
        if (id != null) {
            if (!map.containsKey(id)) {
                ModulesNetwork network = new ModulesNetwork();
                network.id = id;
                map.put(id, network);
                return network;
            }
            return map.get(id);
        }
        return null;
    }

    public static void removeNetwork(Level level, ModulesNetwork network) {
        ControlPanels.LOGGER.debug("Removed network {}", network.id);
        ModulesNetworkManager.ALL.get(level).remove(network.id);
    }

    public static void handleAddingMember(ModulesNetworkMember member) {
        BlockPos pos = member.getBlockPos();
        Level level = member.getLevel();
        if (level.isClientSide)
            return;
        if (!level.isLoaded(pos))
            return;

        List<ModulesNetwork> surroundingNetworks = new ArrayList<>();
        for (ModulesNetworkMember neighbor : getNeighbors(member)) {
            ModulesNetwork network = getNetwork(neighbor);
            ControlPanels.LOGGER.debug(String.valueOf(network==null));
            if (network == null)
                continue;
            surroundingNetworks.add(network);
        }

        ControlPanels.LOGGER.debug(String.valueOf(surroundingNetworks.size()));

        ModulesNetwork network;
        if (surroundingNetworks.isEmpty()) {
            network = new ModulesNetwork();
            network.id = UUID.randomUUID();
            network.addMember(member);
            network.syncMemberIds();
        } else {
            network = surroundingNetworks.getFirst();
            if (surroundingNetworks.size() > 1) {
                surroundingNetworks.removeFirst();
                network.merge(new HashSet<>(surroundingNetworks));
                for (ModulesNetwork neighborNetwork : surroundingNetworks)
                    removeNetwork(level, neighborNetwork);
            }
            network.addMember(member);
            network.syncMemberIds();
        }
    }

    public static Set<ModulesNetworkMember> getNeighbors(ModulesNetworkMember member) {
        BlockPos pos = member.getBlockPos();
        Level level = member.getLevel();
        Set<ModulesNetworkMember> ret = new HashSet<>();
        for (Direction direction : Direction.values()) {
            BlockEntity other = level.getBlockEntity(pos.relative(direction));
            if (!(other instanceof ModulesNetworkMember otherMember))
                continue;
            if (isConnected(member, otherMember))
                ret.add(otherMember);
        }
        return ret;
    }

    public static boolean isConnected(ModulesNetworkMember from, ModulesNetworkMember to) {
        BlockState fromState = from.getBlockState();
        BlockState toState = to.getBlockState();
        return from.isConnected(to, fromState, toState) && to.isConnected(from, toState, fromState);
    }

    public static boolean hasNetwork(Level level, ModulesNetwork network) {
        for (ModulesNetwork other : ALL.get(level).values()) {
            if (network.equals(other))
                return true;
        }
        return false;
    }
}