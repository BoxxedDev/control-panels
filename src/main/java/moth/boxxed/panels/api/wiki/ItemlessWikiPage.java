package moth.boxxed.panels.api.wiki;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public class ItemlessWikiPage implements IWikiPage {
    private final String titleTranslation;
    private final List<Paragraph> paragraphs = new ArrayList<>();

    private WikiCategory category = WikiCategory.MISC;
    private int priority = -1;

    protected ItemlessWikiPage(String titleTranslation) {
        this.titleTranslation = titleTranslation;
    }

    public static ItemlessWikiPage of(String titleTranslation) {
        return new ItemlessWikiPage(titleTranslation);
    }

    public ItemlessWikiPage category(WikiCategory category) {
        this.category = category;
        return this;
    }

    public ItemlessWikiPage addParagraph(String paragraph) {
        this.paragraphs.add(
                new IWikiPage.Paragraph(
                        "paragraph_%d".formatted(this.paragraphs.size()),
                        paragraph
                )
        );
        return this;
    }

    public ItemlessWikiPage addParagraph(String paragraph, String customFallback) {
        this.paragraphs.add(
                new IWikiPage.Paragraph(
                        customFallback,
                        paragraph
                )
        );
        return this;
    }

    public ItemlessWikiPage setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(this.titleTranslation);
    }

    @Override
    public ItemLike getItemlike() {
        return Items.AIR;
    }

    @Override
    public WikiCategory getCategory() {
        return this.category;
    }

    @Override
    public Component getParagraph(int index) {
        if (index >= this.getParagraphs())
            return Component.empty();

        IWikiPage.Paragraph paragraph = this.paragraphs.get(index);
        return Component.translatableWithFallback(
                "%s.%s".formatted(this.titleTranslation, paragraph.fallback()),
                paragraph.paragraph()
        );
    }

    @Override
    public int getParagraphs() {
        return this.paragraphs.size();
    }

    @Override
    public int getSidebarPriority() {
        return this.priority;
    }
}
