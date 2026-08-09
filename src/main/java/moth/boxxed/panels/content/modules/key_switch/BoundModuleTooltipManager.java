package moth.boxxed.panels.content.modules.key_switch;

import moth.boxxed.panels.index.PanelDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BoundModuleTooltipManager {
    public static void addTooltip(List<Component> toolTip, ItemStack itemStack) {
        if (itemStack.has(PanelDataComponents.BOUND_MODULE)) {
            BlockPos pos = itemStack.get(PanelDataComponents.BOUND_MODULE).pos();
            String posString = "(%d, %d, %d)".formatted(pos.getX(), pos.getY(), pos.getZ());

            toolTip.add(1, Component.translatable("tooltip.dashpanels.key.bound_pos", posString));
            toolTip.add(1, Component.translatable(
                    "tooltip.dashpanels.key.bound_id",
                    itemStack.get(PanelDataComponents.BOUND_MODULE).uuid().toString()
            ));
            toolTip.add(1, Component.literal("[i]").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        }
    }
}
