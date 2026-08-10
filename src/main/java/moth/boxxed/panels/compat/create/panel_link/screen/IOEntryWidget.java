package moth.boxxed.panels.compat.create.panel_link.screen;

import com.simibubi.create.AllSoundEvents;
import moth.boxxed.panels.api.module.io.IOEntry;
import moth.boxxed.panels.api.module.io.ModuleIOType;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class IOEntryWidget extends AbstractSimiWidget {
    private List<IOEntry> available;
    private int currentIndex = 0;
    private int priorIndex = 0;

    public IOEntryWidget(int x, int y, int width, int height, Collection<IOEntry> available, Component message) {
        super(x, y, width, height, message);
        this.setAvailable(available);
    }

    public void setAvailable(Collection<IOEntry> available) {
        this.available = new ArrayList<>(available);
        this.available.sort(Comparator.comparing(IOEntry::name));
    }

    public void setCurrentEntry(IOEntry entry) {
        if (this.available.contains(entry)) {
            this.currentIndex = this.available.indexOf(entry);
            return;
        }
        this.currentIndex = -1;
    }

    public IOEntry getCurrent() {
        if (this.currentIndex == -1) {
            return null;
        }
        if (this.available.isEmpty())
            return null;
        return this.available.get(this.currentIndex);
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
        if (this.currentIndex >= 0 && this.getCurrent() != null) {
            graphics.enableScissor(this.getX()+2, this.getY(), this.getX()+this.getWidth()-2, this.getY()+this.getHeight());
            graphics.drawScrollingString(font, Component.literal(this.getCurrent().toString()), this.getX() + 2, this.getX()+this.getWidth()-2, y, 0xFFFFFF);
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
                IOEntry entry = this.available.get(i);

                String prefix = i == this.currentIndex ? "-> " : "   ";
                ChatFormatting formatting = i == this.currentIndex ? ChatFormatting.WHITE : ChatFormatting.GRAY;

                Component suffix = entry.type() == ModuleIOType.INPUT ^ entry.type() == ModuleIOType.MULTI_INPUT ?
                        Component.literal("I").withStyle(ChatFormatting.GREEN) :
                        Component.literal("O").withStyle(ChatFormatting.RED);

                list.add(Component.literal(prefix + entry + " | ").withStyle(formatting).append(suffix));
            }
            if (this.currentIndex+3 < this.available.size()) {
                list.add(Component.literal("   ...").withStyle(ChatFormatting.GRAY));
            }

            return list;
        }

        return super.getToolTip();
    }
}
