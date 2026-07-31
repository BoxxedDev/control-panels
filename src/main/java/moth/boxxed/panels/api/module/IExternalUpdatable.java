package moth.boxxed.panels.api.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface IExternalUpdatable {
    void update(ServerPlayer player, CompoundTag tag, HolderLookup.Provider registries);
}