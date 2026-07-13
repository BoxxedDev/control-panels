package moth.boxxed.panels.util;

import net.mcexpanded.fancytabsections.Section.Section;
import net.mcexpanded.fancytabsections.creativetab.ConglomerateOfItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record CustomSectionTextured(ResourceLocation id, Component title, ResourceLocation texture, int textColor, boolean textShadow, boolean collapsible, ConglomerateOfItems items) implements Section {
    public static CustomSectionTextured of(ResourceLocation id, Component title, int textColor, boolean textShadow, boolean collapsible, ConglomerateOfItems items)
    {
        return new CustomSectionTextured(
                id,
                title,
                ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/gui/fancy_tab_section/" + id.getPath() + ".png"),
                textColor,
                textShadow,
                collapsible,
                items
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, int topLeftX, int topLeftY)
    {
        guiGraphics.blit(texture, topLeftX, topLeftY, 162, 18, 0, 0, 162, 18, 162, 18);

        guiGraphics.drawString(font, title, topLeftX + 4, topLeftY + 5, textColor, textShadow);
    }
}
