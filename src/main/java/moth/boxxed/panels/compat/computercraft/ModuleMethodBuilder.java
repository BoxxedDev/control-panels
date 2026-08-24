package moth.boxxed.panels.compat.computercraft;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.registry.ModulesRegistry;

import java.util.*;

public class ModuleMethodBuilder {
    private final Map<String, ReturnMethod<?>> returnMethods = new Object2ObjectOpenHashMap<>();
    private final Map<String, VoidMethod> voidMethods = new Object2ObjectOpenHashMap<>();
    private final Module module;

    //Build the default methods on init, also gonna check in the actual build methods so developers won't override default methods and stuff
    public ModuleMethodBuilder(Module module) {
        this.module = module;
        returnMethods.put(
                "getType",
                args -> ModulesRegistry.MODULE_REGISTRY.getKey(module.type).getPath()
        );
        returnMethods.put(
                "getName",
                args -> module.getName()
        );
        returnMethods.put(
                "getX",
                args -> module.getPos().x
        );
        returnMethods.put(
                "getY",
                args -> module.getPos().y
        );
        returnMethods.put(
                "getSizeX",
                args -> module.getSize().x
        );
        returnMethods.put(
                "getSizeY",
                args -> module.getSize().y
        );

        //uhhh i think this should return every method including ones added later since it should be pointing to the two maps
        returnMethods.put(
                "getMethods",
                args -> new ObjectArrayList<>(returnMethods.keySet()).addAll(voidMethods.keySet())
        );
    }

    public <T> void addReturn(String methodName, ReturnMethod<T> returnMethod) {
        if (this.voidMethods.containsKey(methodName) || this.returnMethods.containsKey(methodName)) {
            throw new IllegalArgumentException("Method is already declared in %s".formatted(this.module.getName()));
        }
        this.returnMethods.put(methodName, returnMethod);
    }

    public void addVoid(String methodName, VoidMethod voidMethod) {
        if (this.voidMethods.containsKey(methodName) || this.returnMethods.containsKey(methodName)) {
            throw new IllegalArgumentException("Method is already declared in %s".formatted(this.module.getName()));
        }
        this.voidMethods.put(methodName, voidMethod);
    }

    public Map<String, ReturnMethod<?>> getReturnMethods() {
        return this.returnMethods;
    }

    public Map<String, VoidMethod> getVoidMethods() {
        return this.voidMethods;
    }

    public List<String> getAllMethodNames() {
        List<String> ret = new ObjectArrayList<>();
        ret.addAll(this.returnMethods.keySet());
        ret.addAll(this.voidMethods.keySet());
        ret.sort(String::compareTo);
        return ret;
    }

    @FunctionalInterface
    public interface ReturnMethod<T> {
        T get(IModuleArguments args) throws ModuleLuaException;
    }

    @FunctionalInterface
    public interface VoidMethod {
        void run(IModuleArguments args) throws ModuleLuaException;
    }
}
