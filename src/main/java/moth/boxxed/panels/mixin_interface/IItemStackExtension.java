package moth.boxxed.panels.mixin_interface;

import moth.boxxed.panels.index.PanelTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

public interface IItemStackExtension extends net.neoforged.neoforge.common.extensions.IItemStackExtension {
    private ItemStack self() {
        return (ItemStack)(Object) this;
    }

    @Override
    default boolean doesSneakBypassUse(LevelReader level, BlockPos pos, Player player) {
        if (self().is(PanelTags.Items.WRENCH)) {
            return true;
        }
        return net.neoforged.neoforge.common.extensions.IItemStackExtension.super.doesSneakBypassUse(level, pos, player);
    }
}
