package moth.boxxed.panels.mixin;

import moth.boxxed.panels.mixin_interface.IItemStackExtension;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStackExtension {}
