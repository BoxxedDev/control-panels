package moth.boxxed.panels.api.module.config.gui.widgets;

import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public interface ConfigFrameWidget<T> extends GuiEventListener, NarratableEntry {
    void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    int getWidth();
    int getHeight();

    void setX(int x);
    void setY(int y);

    default void onRemove() {}
}
