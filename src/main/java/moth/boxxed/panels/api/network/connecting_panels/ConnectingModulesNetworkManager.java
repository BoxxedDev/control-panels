package moth.boxxed.panels.api.network.connecting_panels;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.cable.CableBlockEntity;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlock;
import moth.boxxed.panels.content.cable.stripped.StrippedCableBlockEntity;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.*;

public class ConnectingModulesNetworkManager {
    public static final Map<LevelAccessor, Map<UUID, ConnectingModulesNetwork>> ALL = new HashMap<>();

    @SubscribeEvent
    public static void onLoad(LevelEvent.Load event) {
        ALL.put(event.getLevel(), new HashMap<>());
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        ALL.remove(event.getLevel());
    }

    public static ConnectingModulesNetwork getOrCreate(BlockEntity be) {
        if (!(be instanceof INetworkMember member)) return null;

        Level level = be.getLevel();
        Map<UUID, ConnectingModulesNetwork> map = ALL.computeIfAbsent(level, $ -> new HashMap<>());

        if (member.getNetwork() != null) {
            UUID id = member.getNetwork();
            ConnectingModulesNetwork network = map.computeIfAbsent(id, $ -> new ConnectingModulesNetwork(level, id));
            network.addMember(be.getBlockPos());
            return network;
        }

        BlockPos pos = be.getBlockPos();
        List<ConnectingModulesNetwork> surroundingNetworks = new ArrayList<>();

        //Handle panel and how it connects
        if (be instanceof PanelBlockEntity pbe) {
            BlockState state = pbe.getBlockState();
            Direction blockDirection = state.getValue(PanelBlock.FACING);

            BlockState leftState = level.getBlockState(pos.relative(blockDirection.getClockWise()));
            BlockState rightState = level.getBlockState(pos.relative(blockDirection.getCounterClockWise()));
            BlockState backState = level.getBlockState(pos.relative(blockDirection.getOpposite()));

            boolean left = leftState.getBlock() instanceof PanelBlock && leftState.getValue(PanelBlock.FACING) == blockDirection;
            boolean right = rightState.getBlock() instanceof PanelBlock && rightState.getValue(PanelBlock.FACING) == blockDirection;
            boolean back = backState.getBlock() instanceof CableBlock;

            if (left && level.getBlockEntity(pos.relative(blockDirection.getClockWise())) instanceof INetworkMember leftMember)
                surroundingNetworks.add(map.get(leftMember.getNetwork()));
            if (right && level.getBlockEntity(pos.relative(blockDirection.getCounterClockWise())) instanceof INetworkMember rightMember)
                surroundingNetworks.add(map.get(rightMember.getNetwork()));
            if (back && level.getBlockEntity(pos.relative(blockDirection.getOpposite())) instanceof INetworkMember backMember)
                surroundingNetworks.add(map.get(backMember.getNetwork()));
        }

        //Handle cable and how it connects
        if (be instanceof CableBlockEntity) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockState neighborState = level.getBlockState(pos.relative(direction));
                if (!(level.getBlockEntity(pos.relative(direction)) instanceof INetworkMember neighborMember)) continue;

                if (neighborState.getBlock() instanceof CableBlock)
                    surroundingNetworks.add(map.get(neighborMember.getNetwork()));
                if ((neighborState.getBlock() instanceof PanelBlock && neighborState.getValue(PanelBlock.FACING)==direction) ||
                        (neighborState.getBlock() instanceof StrippedCableBlock && neighborState.getValue(StrippedCableBlock.FACING)==direction))
                    surroundingNetworks.add(map.get(neighborMember.getNetwork()));
            }
        }

        //Handle stripped cable
        if (be instanceof StrippedCableBlockEntity scbe) {
            BlockState state = scbe.getBlockState();
            Direction blockDirection = state.getValue(StrippedCableBlock.FACING);

            if (level.getBlockState(pos.relative(blockDirection.getOpposite())).getBlock() instanceof CableBlock &&
                level.getBlockEntity(pos.relative(blockDirection.getOpposite())) instanceof INetworkMember backMember) {
                ConnectingModulesNetwork network = map.get(backMember.getNetwork());
                if (network != null)
                    surroundingNetworks.add(network);
            }
        }

        if (surroundingNetworks.isEmpty()) {
            UUID networkId = UUID.randomUUID();
            ConnectingModulesNetwork network = new ConnectingModulesNetwork(level, networkId);
            network.addMember(pos);
            map.put(networkId, network);
            member.setNetwork(networkId);
            ControlPanels.LOGGER.debug("Made new network with ID: {}", networkId);
            return network;
        }

        ConnectingModulesNetwork network = surroundingNetworks.getFirst();
        if (network != null) {
            surroundingNetworks.removeFirst();
            network.merge(surroundingNetworks);
            network.addMember(pos);
            member.setNetwork(network.id);
            ControlPanels.LOGGER.debug("Merged and added to network with ID: {}", network.id);
        }
        return network;
    }

    public static ConnectingModulesNetwork get(Level level, UUID networkId) {
        Map<UUID, ConnectingModulesNetwork> map = ALL.computeIfAbsent(level, $ -> new HashMap<>());
        return map.get(networkId);
    }
}