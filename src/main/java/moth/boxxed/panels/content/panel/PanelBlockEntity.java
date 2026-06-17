package moth.boxxed.panels.content.panel;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.cable.CableBlock;
import moth.boxxed.panels.content.panel.screen.PanelMenu;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.index.PanelBlocks;
import moth.boxxed.panels.util.Rect2d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PanelBlockEntity extends AbstractPanelBlockEntity {
    public ModuleMap modules;
    public SimpleContainer container;

    private Map<Player, String> selectedModules = new HashMap<>();

    public PanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(PanelType.DEFAULT, PanelBlockEntities.PANEL.get(), pos, blockState);
        this.modules = new ModuleMap();
        //12*16 is 192 so like, I think that would be the max size.
        this.container = new SimpleContainer(192);
    }

    @Override
    public boolean isConnected(ModulesNetworkMember other, BlockState from, BlockState to) {
        if (!(from.getBlock() instanceof PanelBlock)) return false;

        BlockPos otherPos = other.getBlockPos();
        BlockPos pos = getBlockPos();
        BlockPos delta = otherPos.subtract(pos);
        Direction direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());
        Direction fromDirection = from.getValue(PanelBlock.FACING);

        if (direction.equals(Direction.UP))
            return false;
        if ((fromDirection.getOpposite()==direction || direction==Direction.DOWN) && to.getBlock() instanceof CableBlock)
            return true;
        return (fromDirection.getClockWise()==direction || fromDirection.getCounterClockWise()==direction) &&
                to.getBlock() instanceof PanelBlock &&
                from.getValue(PanelBlock.FACING) == to.getValue(PanelBlock.FACING);
    }
}
