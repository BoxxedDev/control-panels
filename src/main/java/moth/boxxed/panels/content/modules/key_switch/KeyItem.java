package moth.boxxed.panels.content.modules.key_switch;

import com.google.common.collect.Maps;
import moth.boxxed.panels.index.PanelDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KeyItem extends Item {
    private static final Map<DyeColor, KeyItem> COLOR_KEYS = Maps.newEnumMap(DyeColor.class);
    private final Optional<DyeColor> color;

    public KeyItem(DyeColor dyeColor, Properties properties) {
        super(properties);
        this.color = Optional.ofNullable(dyeColor);
        if (this.color.isPresent()) {
            COLOR_KEYS.put(dyeColor, this);
        }
    }

    public Optional<DyeColor> getColor() {
        return this.color;
    }

    public static KeyItem fromColor(DyeColor color) {
        return COLOR_KEYS.get(color);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> toolTip, TooltipFlag tooltipFlag) {
        if (stack.get(PanelDataComponents.BOUND_MODULE) == null)
            return;

        BlockPos pos = stack.get(PanelDataComponents.BOUND_MODULE).pos();
        String posString = "(%d, %d, %d)".formatted(pos.getX(), pos.getY(), pos.getZ());

        toolTip.add(Component.literal("[i]").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        toolTip.add(Component.translatable(
                "tooltip.dashpanels.key.bound_id",
                stack.get(PanelDataComponents.BOUND_MODULE).uuid().toString()
        ));
        toolTip.add(Component.translatable("tooltip.dashpanels.key.bound_pos", posString));
    }
}
