package moth.boxxed.panels.compat.computercraft.peripherals;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IPeripheral;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.computercraft.IModuleArguments;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.compat.computercraft.ModuleLuaException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;

public class NetworkMemberPeripheral implements IPeripheral {
    private final ModulesNetworkMember blockEntity;
    public NetworkMemberPeripheral(BlockEntity blockEntity) {
        this.blockEntity = (ModulesNetworkMember) blockEntity;
    }

    @Override
    public String getType() {
        return "control_panel";
    }

    @LuaFunction
    public String getNetwork() {
        return blockEntity.network.toString();
    }

    @LuaFunction
    public IDynamicLuaObject getModule(final String moduleName) throws LuaException {
        this.blockEntity.getOrCreate().compileModules();
        var filteredMap = this.blockEntity.getOrCreate().getCompiledModules().asGenericLuaPairMap();
        if (!filteredMap.containsKey(moduleName))
            throw new LuaException("Attached network does not contain module %s".formatted(moduleName));
        return fromModuleLuaObject(filteredMap.get(moduleName).getA(), filteredMap.get(moduleName).getB());
    }

    @LuaFunction
    public Map<String, IDynamicLuaObject> getModules() {
        this.blockEntity.getOrCreate().compileModules();
        Map<String, IDynamicLuaObject> ret = new HashMap<>();
        for (var entry : this.blockEntity.getOrCreate().getCompiledModules().asGenericLuaPairMap().entrySet())
            ret.put(entry.getKey(), fromModuleLuaObject(entry.getValue().getA(), entry.getValue().getB()));
        return ret;
    }

    @LuaFunction
    public Map<String, IDynamicLuaObject> getModulesOfType(final String typeName) {
        this.blockEntity.getOrCreate().compileModules();
        Map<String, IDynamicLuaObject> ret = new HashMap<>();
        for (var entry : this.blockEntity.getOrCreate().getCompiledModules().asGenericLuaPairMap().entrySet()) {
                ResourceLocation location = ModulesRegistry.MODULE_REGISTRY.getKey(entry.getValue().getA().type);
                if (location != null && location.equals(ResourceLocation.tryParse(typeName)))
                    ret.put(entry.getKey(), fromModuleLuaObject(entry.getValue().getA(), entry.getValue().getB()));
        }
        return ret;
    }

    @LuaFunction
    public List<String> getAllModuleTypes() {
        List<String> ret = new ArrayList<>();
        for (ResourceLocation location : ModulesRegistry.MODULE_REGISTRY.keySet())
            ret.add(location.getPath());
        return ret;
    }

    @LuaFunction
    public IDynamicLuaObject getModuleAt(int x, int y) {
        this.blockEntity.getOrCreate().compileModules();
        for (Map.Entry<String, Pair<Module, IModuleLuaObject>> entry : this.blockEntity.getOrCreate().getCompiledModules().asGenericLuaPairMap().entrySet()) {
            if (Objects.equals(entry.getValue().getA().getPos(), new Vector2i(x, y))) {
                return fromModuleLuaObject(entry.getValue().getA(), entry.getValue().getB());
            }
        }
        return null;
    }

    private static IDynamicLuaObject fromModuleLuaObject(Module module, IModuleLuaObject luaObject) {
        return new IDynamicLuaObject() {
            @Override
            public String[] getMethodNames() {
                List<String> names = new ArrayList<>();
                BiConsumer<String, IModuleLuaObject.ReturnMethod<?>> input = ((string, returnMethod) -> names.add(string));

                luaObject.getMethods(input);
                luaObject.getDefaultMethods(names, input);

                return names.toArray(new String[0]);
            }

            @Override
            public MethodResult callMethod(ILuaContext context, int method, IArguments arguments) throws LuaException {
                List<IModuleLuaObject.ReturnMethod<?>> methods = new ArrayList<>();
                List<String> names = new ArrayList<>();
                BiConsumer<String, IModuleLuaObject.ReturnMethod<?>> input = ((string, returnMethod) -> {
                    methods.add(returnMethod);
                    names.add(string);
                });

                luaObject.getMethods(input);
                luaObject.getDefaultMethods(names, input);

                Object ret = methods.get(method).get(new IModuleArguments() {
                    @Override
                    public int count() {
                        return arguments.count();
                    }

                    @Override
                    public @Nullable Object get(int index) {
                        try {
                            return arguments.get(index);
                        } catch (LuaException e) {
                            return null;
                        }
                    }

                    @Override
                    public String getType(int index) {
                        return arguments.getType(index);
                    }

                    //TODO: Make this work (I don't really know what it does so it should be fine right now)
                    @Override
                    public IModuleArguments drop(int count) {
                        return null;
                    }
                });

                if (ret instanceof ModuleLuaException luaException) {
                    if (luaException.hasLevel()) {
                        throw new LuaException(luaException.getMessage(), luaException.getLevel());
                    } else {
                        throw new LuaException(luaException.getMessage());
                    }
                }

                module.parentBlockEntity.setChanged();
                module.parentBlockEntity.blockChanged();

                return MethodResult.of(ret);
            }
        };
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }
}
