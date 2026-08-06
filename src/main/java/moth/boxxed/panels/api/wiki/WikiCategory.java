package moth.boxxed.panels.api.wiki;

import net.minecraft.network.chat.Component;

public record WikiCategory(String category) {
    /**
    Default wiki category, isn't put under a drop down like other categories
     */
    public static final WikiCategory MISC = new WikiCategory("misc");

    public Component getComponent() {
        return Component.translatable("dashpanels.wiki.category.%s".formatted(category));
    }

    public boolean isMisc() {
        return this == MISC;
    }
}
