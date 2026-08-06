package moth.boxxed.panels.api.wiki;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import joptsimple.internal.Strings;
import moth.boxxed.panels.index.PanelKeybinds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WikiTooltipManager {
    private static float holdValue = 0;
    private static ItemStack hoveringItem = ItemStack.EMPTY;
    private static ItemStack previousItem = ItemStack.EMPTY;
    private static boolean deferTick = false;

    public static void tick() {
        deferTick = true;
        holdValue = Math.max(0, holdValue - 0.05f);
    }

    public static void deferredTick() {
        deferTick = false;
        if (hoveringItem.isEmpty() || !WikiableEntries.existsForItem(hoveringItem)) {
            hoveringItem = ItemStack.EMPTY;
            holdValue = 0;
            return;
        }

        boolean isKeyDown = InputConstants.isKeyDown(
                Minecraft.getInstance().getWindow().getWindow(),
                PanelKeybinds.HOLD_TO_OPEN_WIKI.getKey().getValue()
        ) && PanelKeybinds.HOLD_TO_OPEN_WIKI.isConflictContextAndModifierActive();
        if (isKeyDown && Minecraft.getInstance().screen != null && RenderSystem.isOnRenderThread()) {
            holdValue = Math.clamp(holdValue + 0.25f, 0, 1);
        }

        if (holdValue >= 1) {
            WikiableEntries.openForItem(hoveringItem);
        }
    }

    public static void addTooltip(List<Component> toolTip, ItemStack itemStack) {
        previousItem = hoveringItem;
        hoveringItem = itemStack;

        if (hoveringItem != previousItem) {
            holdValue = 0;
            return;
        }

        if (deferTick)
            deferredTick();

        if (!WikiableEntries.existsForItem(itemStack))
            return;

        if (toolTip.size() < 2) {
            toolTip.add(progressBar());
        } else {
            toolTip.add(1, progressBar());
        }
    }

    private static Component progressBar() {
        Component ret = Component.translatable("dashpanels.wiki.hold_to_open",
                PanelKeybinds.HOLD_TO_OPEN_WIKI.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.LIGHT_PURPLE)
        ).withStyle(ChatFormatting.DARK_PURPLE);

        if (holdValue > 0) {
            char barThing = '╏';
            Font font = Minecraft.getInstance().font;
            float barWidth = font.width(String.valueOf(barThing));
            float holdTipWidth = font.width(ret);

            int full = (int) (holdTipWidth / barWidth);
            int fillAmount = (int) (holdValue * full);

            StringBuilder bars = new StringBuilder();
            bars.append(ChatFormatting.LIGHT_PURPLE).append(Strings.repeat(barThing, fillAmount));
            if (holdValue < 1) {
                bars.append(ChatFormatting.DARK_PURPLE).append(Strings.repeat(barThing, full - fillAmount));
            }
            ret = Component.literal(bars.toString());
        }
        return ret;
    }
}
