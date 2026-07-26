package moth.boxxed.panels.compat.computercraft.peripherals;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IPeripheral;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.computercraft.IModuleArguments;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        Map<String, IModuleLuaObject> filteredMap = this.blockEntity.getOrCreate().getCompiledModules().asGenericLuaMap();
        if (!filteredMap.containsKey(moduleName))
            throw new LuaException("Attached network does not contain moduleName %s".formatted(moduleName));
        return fromModuleLuaObject(filteredMap.get(moduleName));
    }

    @LuaFunction
    public List<IDynamicLuaObject> getModules() {
        this.blockEntity.getOrCreate().compileModules();
        List<IDynamicLuaObject> ret = new ArrayList<>();
        for (Map.Entry<String, IModuleLuaObject> entry : this.blockEntity.getOrCreate().getCompiledModules().asGenericLuaMap().entrySet())
            ret.add(fromModuleLuaObject(entry.getValue()));
        return ret.reversed();
    }

    @LuaFunction
    public List<IDynamicLuaObject> getModulesOfType(final String typeName) {
        this.blockEntity.getOrCreate().compileModules();
        List<IDynamicLuaObject> ret = new ArrayList<>();
        for (Map.Entry<String, IModuleLuaObject> entry : this.blockEntity.getOrCreate().getCompiledModules().asGenericLuaMap().entrySet()) {
            if (entry.getValue() instanceof Module module) {
                ResourceLocation location = ModulesRegistry.MODULE_REGISTRY.getKey(module.type);
                if (location != null && location.getPath().equals(typeName))
                    ret.add(fromModuleLuaObject(entry.getValue()));
            }
        }
        return ret.reversed();
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
                return fromModuleLuaObject(entry.getValue().getB());
            }
        }
        return null;
    }

    private static IDynamicLuaObject fromModuleLuaObject(IModuleLuaObject luaObject) {
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

                return MethodResult.of(methods.get(method).get(new IModuleArguments() {
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
                }));
            }
        };
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }
}
