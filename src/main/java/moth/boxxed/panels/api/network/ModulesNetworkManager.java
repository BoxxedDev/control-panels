package moth.boxxed.panels.api.network;

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
        if (member.network == null) return null;
        Map<UUID, ModulesNetwork> map = ALL.computeIfAbsent(member.getLevel(), $ -> new HashMap<>());
        return map.computeIfAbsent(member.network, uuid -> new ModulesNetwork(member.getLevel(), uuid));
    }

    public static void removeNetwork(ModulesNetwork network) {
        ModulesNetworkManager.ALL.get(network.level).remove(network.id);
    }

    public static void handleAddingMember(ModulesNetworkMember member) {
        if (member.hasNetwork()) {
            getNetwork(member).addMember(member);
            member.setNetwork(member.network);
            return;
        }

        Set<ModulesNetwork> neighbors = new HashSet<>();
        for (ModulesNetworkMember neighbor : getNeighbors(member)) {
            ModulesNetwork network = getNetwork(neighbor);
            if (network == null) continue;
            neighbors.add(network);
        }

        if (neighbors.isEmpty()) {
            member.setNetwork(UUID.randomUUID());
        } else {
            ModulesNetwork first = neighbors.iterator().next();
            neighbors.remove(first);
            first.merge(neighbors);
            member.setNetwork(first.id);
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
            if (isConnected(member, otherMember, direction)) {
                ret.add(otherMember);
            }
        }
        return ret;
    }

    public static boolean isConnected(ModulesNetworkMember from, ModulesNetworkMember to, Direction direction) {
        BlockState fromState = from.getBlockState();
        if (!(fromState.getBlock() instanceof ModulesNetworkMemberBlock fromBlock))
            return false;
        BlockState toState = to.getBlockState();
        if (!(toState.getBlock() instanceof ModulesNetworkMemberBlock toBlock))
            return false;
        return fromBlock.isConnecting(from.getLevel(), from.getBlockPos(), fromState, direction) && toBlock.isConnecting(to.getLevel(), to.getBlockPos(), toState, direction.getOpposite());
    }

    public static boolean hasNetwork(ModulesNetwork network) {
        return ALL.get(network.level).values().contains(network);
    }
}