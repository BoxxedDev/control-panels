package moth.boxxed.panels.compat.computercraft.peripherals;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.computercraft.IModuleArguments;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.compat.computercraft.ModuleLuaException;
import moth.boxxed.panels.compat.computercraft.ModuleMethodBuilder;
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

    //TODO: refactor alllll this to remove most references of IModuleLuaObject
    @LuaFunction
    public IDynamicLuaObject getModule(final String moduleName) throws LuaException {
        this.blockEntity.getOrCreate().compileModules();
        var networkModules = this.blockEntity.getOrCreate().getCompiledModules();

//        var filteredMap = this.blockEntity.getOrCreate().getCompiledModules().asGenericLuaPairMap();
        if (!networkModules.containsKey(moduleName))
            throw new LuaException("Attached network does not contain module %s".formatted(moduleName));
        return dynamicLuaObject(networkModules.get(moduleName));
    }

    @LuaFunction
    public Map<String, IDynamicLuaObject> getModules() {
        this.blockEntity.getOrCreate().compileModules();
        Map<String, IDynamicLuaObject> ret = new HashMap<>();
        for (var entry : this.blockEntity.getOrCreate().getCompiledModules().entrySet())
            ret.put(entry.getKey(), dynamicLuaObject(entry.getValue()));
        return ret;
    }

    @LuaFunction
    public Map<String, IDynamicLuaObject> getModulesOfType(final String typeName) {
        this.blockEntity.getOrCreate().compileModules();
        Map<String, IDynamicLuaObject> ret = new HashMap<>();
        for (var entry : this.blockEntity.getOrCreate().getCompiledModules().entrySet()) {
                ResourceLocation location = ModulesRegistry.MODULE_REGISTRY.getKey(entry.getValue().type);
                if (location != null && location.equals(ResourceLocation.tryParse(typeName)))
                    ret.put(entry.getKey(), dynamicLuaObject(entry.getValue()));
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
        for (var entry : this.blockEntity.getOrCreate().getCompiledModules().entrySet()) {
            if (Objects.equals(entry.getValue().getPos(), new Vector2i(x, y))) {
                return dynamicLuaObject(entry.getValue());
            }
        }
        return null;
    }

    @Override
    public void attach(IComputerAccess computer) {
        this.blockEntity.getOrCreate().compileModules();
        for (Module module : this.blockEntity.getOrCreate().compiledModules.values()) {
            module.getComputerHandler().attach(computer);
        }
    }

    @Override
    public void detach(IComputerAccess computer) {
        this.blockEntity.getOrCreate().compileModules();
        for (Module module : this.blockEntity.getOrCreate().compiledModules.values()) {
            module.getComputerHandler().detach(computer);
        }
    }

    private static IDynamicLuaObject dynamicLuaObject(Module module) {
        if (module instanceof IModuleLuaObject luaObject) {
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

        return new IDynamicLuaObject() {
            @Override
            public String[] getMethodNames() {
                ModuleMethodBuilder builder = new ModuleMethodBuilder(module);
                module.buildComputerMethods(builder);

                return builder.getAllMethodNames().toArray(new String[0]);
            }

            @Override
            public MethodResult callMethod(ILuaContext context, int method, IArguments arguments) throws LuaException {
                ModuleMethodBuilder builder = new ModuleMethodBuilder(module);
                module.buildComputerMethods(builder);

                IModuleArguments args = new IModuleArguments() {
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
                };

                String methodName = builder.getAllMethodNames().get(method);

                try {
                    if (builder.getReturnMethods().containsKey(methodName)) {
                        Object ret = builder.getReturnMethods().get(methodName).get(args);
                        module.parentBlockEntity.setChanged();
                        module.parentBlockEntity.blockChanged();
                        return MethodResult.of(ret);
                    } else if (builder.getVoidMethods().containsKey(methodName)) {
                        builder.getVoidMethods().get(methodName).run(args);
                        module.parentBlockEntity.setChanged();
                        module.parentBlockEntity.blockChanged();
                        return MethodResult.of();
                    }
                } catch (ModuleLuaException luaException) {
                    if (luaException.hasLevel()) {
                        throw new LuaException(luaException.getMessage(), luaException.getLevel());
                    } else {
                        throw new LuaException(luaException.getMessage());
                    }
                }

                throw new LuaException("Failed to run method %s for module %s".formatted(methodName, module.getName()));
            }
        };
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }
}
