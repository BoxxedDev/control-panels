package moth.boxxed.panels.api.module.placement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record PlacementContext(Player player, ItemStack inHandStack, boolean isMoving) {
}
