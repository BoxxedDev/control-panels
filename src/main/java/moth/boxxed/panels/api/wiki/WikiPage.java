package moth.boxxed.panels.api.wiki;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WikiPage {
    private final Supplier<? extends ItemLike> itemLikeSupplier;
    private final List<Paragraph> paragraphs = new ArrayList<>();

    private WikiCategory category = WikiCategory.MISC;

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
                new Paragraph(
                        "paragraph_%d".formatted(this.paragraphs.size()),
                        paragraph
                )
        );
        return this;
    }

    public WikiPage addParagraph(String paragraph, String customFallback) {
        this.paragraphs.add(
                new Paragraph(
                        customFallback,
                        paragraph
                )
        );
        return this;
    }

    public Component getTitle() {
        return this.itemLikeSupplier.get().asItem().getDescription();
    }

    public ItemLike getItemlike() {
        return this.itemLikeSupplier.get();
    }

    public WikiCategory getCategory() {
        return this.category;
    }

    public Component getParagraph(int index) {
        if (index >= this.getParagraphs())
            return Component.empty();

        Paragraph paragraph = this.paragraphs.get(index);
        return Component.translatableWithFallback(
                "%s.%s".formatted(this.itemLikeSupplier.get().asItem().getDescriptionId(), paragraph.fallback),
                paragraph.paragraph
        );
    }

    public int getParagraphs() {
        return this.paragraphs.size();
    }

    public record Paragraph(String fallback, String paragraph) {}
}
