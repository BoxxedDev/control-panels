package moth.boxxed.panels.api.module;

import moth.boxxed.panels.api.module.io.IInput;
import moth.boxxed.panels.api.module.io.IMultiInput;
import moth.boxxed.panels.api.module.io.IMultiOutput;
import moth.boxxed.panels.api.module.io.IOutput;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleType<T extends Module> {
    private static final Map<Item, List<ModuleType<?>>> typeItemMap = new HashMap<>();
    private static final Map<ModuleType<?>, Item> itemTypeMap = new HashMap<>();
    public ModuleSupplier<T> factory;
    public Item associatedItem;

    public static ResourceLocation getKey(ModuleType<?> type) {
        return ModulesRegistry.MODULE_REGISTRY.getKey(type);
    }

    public static String getName(ModuleType<?> type) {
        return getKey(type).getPath();
    }

    public ModuleType(ModuleSupplier<T> factory, Item associatedItem) {
        this.factory = factory;
        if (associatedItem == null)
            throw new RuntimeException("Associated Item Cannot Be Null");
//        if (typeItemMap.containsKey(associatedItem))
//            throw new RuntimeException("Associated Item Is Already Registered");

        this.associatedItem = associatedItem;
        typeItemMap.computeIfAbsent(associatedItem, item -> new ArrayList<>());
        typeItemMap.get(associatedItem).add(this);
        itemTypeMap.put(this, associatedItem);
    }

    public static Item getItemFromType(ModuleType<?> type) {
        return itemTypeMap.get(type);
    }

    public T create(int x, int y) {
        return this.factory.create(x,y);
    }

    public static <T extends Item> List<ModuleType<?>> getTypeFromItem(T item) {
        return typeItemMap.get(item);
    }

    public static <T extends Item> boolean isRegisteredModule(T item) {
        return typeItemMap.containsKey(item);
    }

    @FunctionalInterface
    public interface ModuleSupplier<T extends Module> {
        T create(int x, int y);
    }
}
