package moth.boxxed.panels.api.wiki;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;

public class WikiableEntries {
    private static final Map<ResourceLocation, ResourceLocation> OTHER_MAP = new HashMap<>();
    private static final Map<ResourceLocation, IWikiPage> MAP = new HashMap<>();
    private static final Map<WikiCategory, List<IWikiPage>> CATEGORY_PAGES_MAP = new HashMap<>();

    public static void openForItem(ItemStack itemStack) {
        if (!existsForItem(itemStack))
            return;
        if (pageForItem(itemStack) == null)
            return;

        Minecraft.getInstance().tell(() -> {
            WikiScreen wikiScreen = new WikiScreen();
            Minecraft.getInstance().setScreen(wikiScreen);
            wikiScreen.setPage(pageForItem(itemStack));
        });
    }

    public static <T extends IWikiPage> T register(ResourceLocation location, T wikiPage) {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(wikiPage, "Wiki page cannot be null");
        MAP.put(location, wikiPage);
        CATEGORY_PAGES_MAP.computeIfAbsent(
                wikiPage.getCategory(),
                category -> new ArrayList<>()
        ).add(wikiPage);
        return wikiPage;
    }

    public static void registerRedirect(ResourceLocation location, ResourceLocation other) {
        OTHER_MAP.put(location, other);
    }

    public static boolean existsForItem(ItemStack stack) {
        return MAP.containsKey(BuiltInRegistries.ITEM.getKeyOrNull(stack.getItem())) ||
                MAP.containsKey(OTHER_MAP.get(BuiltInRegistries.ITEM.getKeyOrNull(stack.getItem())));
    }

    public static IWikiPage pageForItem(ItemStack stack) {
        ResourceLocation location = BuiltInRegistries.ITEM.getKeyOrNull(stack.getItem());
        if (OTHER_MAP.containsKey(location)) {
            return MAP.get(OTHER_MAP.get(location));
        }

        return MAP.get(location);
    }

    public static Collection<IWikiPage> getAllPages() {
        return MAP.values();
    }

    public static Set<WikiCategory> getAllCategories() {
        return CATEGORY_PAGES_MAP.keySet();
    }

    public static List<IWikiPage> getPagesInCategory(WikiCategory category) {
        return CATEGORY_PAGES_MAP.computeIfAbsent(category, c -> new ArrayList<>());
    }

    public static boolean exists(ResourceLocation location) {
        return MAP.containsKey(location);
    }

    public static IWikiPage pageFor(ResourceLocation location) {
        return MAP.get(location);
    }

    public static void collectLang(String modid, BiConsumer<String, String> keyValueConsumer) {
        List<IWikiPage> pages = MAP.entrySet().stream()
                .filter(entry -> Objects.equals(modid, entry.getKey().getNamespace()))
                .map(Map.Entry::getValue)
                .toList();

        for (IWikiPage wikiPage : pages) {
            for (int i = 0; i < wikiPage.getParagraphs(); i++) {
                IWikiPage.ParagraphTranslation paragraphTranslation = wikiPage.getParagraphTranslation(i);
                keyValueConsumer.accept(
                        paragraphTranslation.key(),
                        paragraphTranslation.value()
                );
            }
        }
    }
}
