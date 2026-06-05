package moth.boxxed.panels.compat.computercraft;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public interface IModuleLuaObject {
    default void getDefaultMethods(List<String> methodNames, BiConsumer<String, ReturnMethod<?>> consumer) {
        if (this instanceof Module module) {
            ResourceLocation location = ModulesRegistry.MODULE_REGISTRY.getKey(module.type);
            if (location != null) {
                consumer.accept(
                        "getType",
                        args -> location.getPath()
                );
            }
            consumer.accept(
                    "getName",
                    args -> module.getName()
            );
            consumer.accept(
                    "getX",
                    args -> module.getPos().x
            );
            consumer.accept(
                    "getY",
                    args -> module.getPos().y
            );
            consumer.accept(
                    "getSizeX",
                    args -> module.getSize().x
            );
            consumer.accept(
                    "getSizeY",
                    args -> module.getSize().y
            );
        }
        consumer.accept(
                "getMethods",
                args -> new ArrayList<>(methodNames)
        );
    }

    /**
     * Gets all custom methods of a module
     * <br>
     * <br>
     * If you want a method to be void just have it return a boolean
     * @param consumer Consumer used to add a new method
     */
    void getMethods(BiConsumer<String, ReturnMethod<?>> consumer);

    @FunctionalInterface
    interface ReturnMethod<T> {
        T get(IModuleArguments args);
    }
}
