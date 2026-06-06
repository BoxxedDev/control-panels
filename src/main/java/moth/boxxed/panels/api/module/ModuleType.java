package moth.boxxed.panels.api.module;

import moth.boxxed.panels.api.registry.ModulesRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

public class ModuleType<T extends Module> {
    private static final Set<Item> ALL_ASSOCIATED_ITEMS = new HashSet<>();
    public ModuleSupplier<T> factory;
    public Item associatedItem;

    public static ResourceLocation getKey(ModuleType<?> type) {
        return ModulesRegistry.MODULE_REGISTRY.getKey(type);
    }

    public ModuleType(ModuleSupplier<T> factory, Item associatedItem) {
        this.factory = factory;
        if (associatedItem == null)
            throw new RuntimeException("Associated Item Cannot Be Null");
        if (ALL_ASSOCIATED_ITEMS.contains(associatedItem))
            throw new RuntimeException("Associated Item Is Already Registered");
        this.associatedItem = associatedItem;
        ALL_ASSOCIATED_ITEMS.add(this.associatedItem);
    }

    public T create(int x, int y) {
        return this.factory.create(x,y);
    }

    @FunctionalInterface
    public interface ModuleSupplier<T extends Module> {
        T create(int x, int y);
    }
}
