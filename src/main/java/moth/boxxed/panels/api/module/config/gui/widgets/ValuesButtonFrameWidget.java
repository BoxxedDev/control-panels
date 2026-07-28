package moth.boxxed.panels.api.module.config.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.config.gui.ModuleConfigScreen;
import moth.boxxed.panels.util.GuiUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.util.Objects;
import java.util.function.Supplier;

public class ValuesButtonFrameWidget<T> extends AbstractWidget implements ConfigFrameWidget<T> {
    protected final ModuleConfigValue<T> configValue;
    protected final Font font;

    protected final Supplier<T[]> valuesSupplier;
    protected final int valuesLength;
    protected int valueIndex;

    public ValuesButtonFrameWidget(ModuleConfigValue<T> configValue, Supplier<T[]> valuesSupplier, Font font, int width, int height, Component message) {
        super(0, 0, width, height, message);
        this.configValue = configValue;
        this.font = font;

        this.valuesSupplier = valuesSupplier;

        T[] values = valuesSupplier.get();
        this.valuesLength = values.length;
        for (int i = 0; i < this.valuesLength; i++) {
            if (Objects.equals(values[i], configValue.get())) {
                this.valueIndex = i;
                break;
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GuiUtil.blitNineSlice(guiGraphics, ModuleConfigScreen.CONFIG_SHEET,
                this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                7, 7, 96, 64,
                256, 256,
                3, 3, 3, 3);

        int textY = this.getY()+(this.getHeight()-this.font.lineHeight)/2;
        String component;
        if (this.configValue.get() instanceof StringRepresentable stringRepresentable) {
            component = stringRepresentable.getSerializedName();
        } else {
            component = String.valueOf(this.configValue.get());
        }
        guiGraphics.drawScrollingString(this.font, Component.nullToEmpty(component), this.getX()+3, this.getX()+this.getWidth()-3, textY, 0xEFFFFF);

        if (this.isMouseOver(mouseX, mouseY)) {
            RenderSystem.enableBlend();
            guiGraphics.setColor(1, 1, 1, 0.25f);
            guiGraphics.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 1, 0xFFFFFFFF);
            guiGraphics.setColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        T[] array = this.valuesSupplier.get();
        this.valueIndex = Mth.positiveModulo(this.valueIndex+1,  this.valuesLength);
        this.configValue.set(array[this.valueIndex]);
    }
}
