package moth.boxxed.panels.compat.create.panel_link.screen;

import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringEntryWidget extends AbstractSimiWidget {
    private List<String> available;
    private int currentIndex = 0;
    private int priorIndex = 0;

    public StringEntryWidget(int x, int y, int width, int height, Collection<String> available, Component message) {
        super(x, y, width, height, message);
        this.setAvailable(available);
    }

    public void setAvailable(Collection<String> available) {
        this.available = new ArrayList<>(available);
        this.available.sort(null);
    }

    public void setCurrentString(String string) {
        if (this.available.contains(string)) {
            this.currentIndex = this.available.indexOf(string);
            return;
        }
        this.currentIndex = -1;
    }

    public String getCurrent() {
        if (this.currentIndex == -1) {
            return null;
        }
        if (this.available.isEmpty())
            return "";
        if (this.available.get(this.currentIndex) != null)
            return this.available.get(this.currentIndex);
        return "";
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        priorIndex = currentIndex;
        this.currentIndex = Math.max(0, Math.min((int) (this.currentIndex-scrollY), this.available.size()-1));

        if (priorIndex != currentIndex)
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(),
                            1.5f + 0.1f * (this.currentIndex) / (this.available.size())));

        return priorIndex != currentIndex;
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        int y = this.getY()+(this.getHeight()/2)-(font.lineHeight);
        if (this.currentIndex >= 0) {
            graphics.enableScissor(this.getX()+2, this.getY(), this.getX()+this.getWidth()-2, this.getY()+this.getHeight());
            graphics.drawScrollingString(font, Component.literal(this.getCurrent()), this.getX() + 2, this.getX()+this.getWidth()-2, y, 0xFFFFFF);
            graphics.disableScissor();
        }
    }

    @Override
    public List<Component> getToolTip() {
        if (this.isHovered()) {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("widget.dashpanels.panel_link.module_select"));

            if (this.currentIndex-2 > 0) {
                list.add(Component.literal("   ...").withStyle(ChatFormatting.GRAY));
            }
            for (int i = Math.max(0, this.currentIndex-2); i < Math.min(this.available.size(), this.currentIndex+3); i++) {
                String string = this.available.get(i);
                String prefix = this.available.indexOf(string) == this.currentIndex ? "-> " : "   ";
                ChatFormatting formatting = this.available.indexOf(string) == this.currentIndex ? ChatFormatting.WHITE : ChatFormatting.GRAY;
                list.add(Component.literal(prefix.concat(string)).withStyle(formatting));
            }
            if (this.currentIndex+3 < this.available.size()) {
                list.add(Component.literal("   ...").withStyle(ChatFormatting.GRAY));
            }

            return list;
        }

        return super.getToolTip();
    }
}
