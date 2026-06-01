package moth.boxxed.panels.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import oshi.util.tuples.Pair;

public class BasicButtonWidget extends AbstractButton {
    public ButtonFunction function;
    public Pair<ResourceLocation, ResourceLocation> spritePair;
    public BasicButtonWidget(ResourceLocation sprite, ResourceLocation hoverSprite, int x, int y, int width, int height, Component message, ButtonFunction function) {
        super(x, y, width, height, message);
        this.function = function;
        this.spritePair = new Pair<>(sprite, hoverSprite);
    }

    @Override
    public void onPress() {
        this.function.perform();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation location = this.spritePair.getA();
        if (this.isHovered())
            location = this.spritePair.getB();

        graphics.blitSprite(location, this.getX(), this.getY(), this.width, this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    public interface ButtonFunction {
        void perform();
    }
}
