package moth.boxxed.panels.api.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface IExternalUpdatable {
    void update(ServerPlayer player, CompoundTag tag, HolderLookup.Provider registries);
}