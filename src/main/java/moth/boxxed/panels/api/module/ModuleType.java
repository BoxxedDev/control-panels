package moth.boxxed.panels.api.module;

import moth.boxxed.panels.api.registry.ModulesRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModuleType<T extends Module> {
    public ModuleSupplier<T> factory;
    public Item associatedItem;

    public static ResourceLocation getKey(ModuleType<?> type) {
        return ModulesRegistry.MODULE_REGISTRY.getKey(type);
    }

    public ModuleType(ModuleSupplier<T> factory, Item associatedItem) {
        this.factory = factory;
        this.associatedItem = associatedItem;
    }

    public T create(int x, int y) {
        return this.factory.create(x,y);
    }

    @FunctionalInterface
    public interface ModuleSupplier<T extends Module> {
        T create(int x, int y);
    }
}
