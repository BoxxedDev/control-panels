package moth.boxxed.panels.api.wiki;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface IWikiPage {
    Component getTitle();

    ItemLike getItemlike();

    WikiCategory getCategory();

    Component getParagraph(int index);

    int getParagraphs();

    int getSidebarPriority();

    record Paragraph(String fallback, String paragraph) {}
}
