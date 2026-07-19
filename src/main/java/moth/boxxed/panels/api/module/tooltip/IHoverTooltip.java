package moth.boxxed.panels.api.module.tooltip;

import moth.boxxed.panels.Dashpanels;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * A hovering tooltip can be added to a module to display an extra information when hovering (other than its name)
 */
public interface IHoverTooltip {
    void addLines(List<Component> list);

    default ResourceLocation tooltipBackgroundSprite() {
        return Dashpanels.path("tooltip/background");
    }
}
