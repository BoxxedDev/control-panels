package moth.boxxed.panels.api.module.config.gui.widgets;

import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class LabelFrameWidget<T> extends AbstractWidget implements ConfigFrameWidget<T> {
    protected final Font font;
    protected final int color;

    public LabelFrameWidget(Font font, Component message, int color) {
        super(0, 0, font.width(message), font.lineHeight, message);
        this.font = font;
        this.color = color;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(this.font, this.getMessage(), this.getX(), this.getY()+this.font.lineHeight/2, this.color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
