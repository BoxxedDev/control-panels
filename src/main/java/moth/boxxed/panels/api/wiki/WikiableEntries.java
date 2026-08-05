package moth.boxxed.panels.api.wiki;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class WikiableEntries {
    private static final Map<ResourceLocation, WikiPage> MAP = new HashMap<>();
    private static final Map<WikiCategory, List<WikiPage>> CATEGORY_PAGES_MAP = new HashMap<>();

    //TODO: Set the wiki page
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

    public static void register(ResourceLocation location, WikiPage wikiPage) {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(wikiPage, "Wiki page cannot be null");
        MAP.put(location, wikiPage);
        CATEGORY_PAGES_MAP.computeIfAbsent(
                wikiPage.getCategory(),
                category -> new ArrayList<>()
        ).add(wikiPage);
    }

    public static boolean existsForItem(ItemStack stack) {
        return MAP.containsKey(BuiltInRegistries.ITEM.getKeyOrNull(stack.getItem()));
    }

    public static WikiPage pageForItem(ItemStack stack) {
        return MAP.get(BuiltInRegistries.ITEM.getKeyOrNull(stack.getItem()));
    }

    public static Collection<WikiPage> getAllPages() {
        return MAP.values();
    }

    public static Set<WikiCategory> getAllCategories() {
        return CATEGORY_PAGES_MAP.keySet();
    }

    public static List<WikiPage> getPagesInCategory(WikiCategory category) {
        return CATEGORY_PAGES_MAP.computeIfAbsent(category, c -> new ArrayList<>());
    }

    public static boolean exists(ResourceLocation location) {
        return MAP.containsKey(location);
    }

    public static WikiPage pageFor(ResourceLocation location) {
        return MAP.get(location);
    }
}
