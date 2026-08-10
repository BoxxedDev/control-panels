package moth.boxxed.panels.api.wiki;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WikiPage implements IWikiPage {
    private final Supplier<? extends ItemLike> itemLikeSupplier;
    private final List<IWikiPage.Paragraph> paragraphs = new ArrayList<>();

    private WikiCategory category = WikiCategory.MISC;
    private int priority = -1;

    protected WikiPage(Supplier<? extends ItemLike> itemSupplier) {
        this.itemLikeSupplier = itemSupplier;
    }

    public static WikiPage of(Supplier<? extends ItemLike> itemSupplier) {
        return new WikiPage(itemSupplier);
    }

    public WikiPage category(WikiCategory category) {
        this.category = category;
        return this;
    }

    public WikiPage addParagraph(String paragraph) {
        this.paragraphs.add(
                new IWikiPage.Paragraph(
                        "paragraph_%d".formatted(this.paragraphs.size()),
                        paragraph
                )
        );
        return this;
    }

    public WikiPage addParagraph(String paragraph, String customFallback) {
        this.paragraphs.add(
                new IWikiPage.Paragraph(
                        customFallback,
                        paragraph
                )
        );
        return this;
    }

    public WikiPage setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public Component getTitle() {
        return this.itemLikeSupplier.get().asItem().getDescription();
    }

    @Override
    public ItemLike getItemlike() {
        return this.itemLikeSupplier.get();
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
                "%s.wiki.%s".formatted(this.itemLikeSupplier.get().asItem().getDescriptionId(), paragraph.translation()),
                paragraph.paragraph()
        );
    }

    @Override
    public ParagraphTranslation getParagraphTranslation(int index) {
        if (index >= this.getParagraphs())
            return null;

        IWikiPage.Paragraph paragraph = this.paragraphs.get(index);
        return new ParagraphTranslation("%s.wiki.%s".formatted(this.itemLikeSupplier.get().asItem().getDescriptionId(), paragraph.translation()), paragraph.paragraph());
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
