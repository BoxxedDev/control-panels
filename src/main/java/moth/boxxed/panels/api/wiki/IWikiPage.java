package moth.boxxed.panels.api.wiki;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public interface IWikiPage {
    Component getTitle();

    ItemLike getItemlike();

    WikiCategory getCategory();

    Component getParagraph(int index);

    ParagraphTranslation getParagraphTranslation(int index);

    int getParagraphs();

    int getSidebarPriority();

    record Paragraph(String translation, String paragraph) {}

    record ParagraphTranslation(String key, String value) {}
}
