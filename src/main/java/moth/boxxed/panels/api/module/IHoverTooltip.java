package moth.boxxed.panels.api.module;

import moth.boxxed.panels.Dashpanels;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IHoverTooltip {
    void addLines(List<Component> list);

    default ResourceLocation tooltipBackgroundSprite() {
        return Dashpanels.path("tooltip/background");
    }
}
