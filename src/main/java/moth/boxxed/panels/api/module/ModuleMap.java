package moth.boxxed.panels.api.module;

import moth.boxxed.panels.api.module.io.*;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;
import oshi.util.tuples.Pair;

import java.util.*;

public class ModuleMap extends LinkedHashMap<String, Module> implements Iterable<Map.Entry<String, Module>> {
    public static ModuleMap empty() {
        return new ModuleMap();
    }

    public void rename(String oldName, String newName) {
        Module module = this.remove(oldName);
        this.put(newName, module);
    }

    public CompoundTag asTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("modules_size", this.size());
        for (int i=0; i<this.size(); i++) {
            Map.Entry<String, Module> moduleEntry = this.asEntryList().get(i);
            CompoundTag subTag = new CompoundTag();
            if (moduleEntry.getValue().saveData(subTag, registries)) {
                subTag.putString("name", moduleEntry.getKey());
                tag.put("module_%d".formatted(i), subTag);
            }
        }
        return tag;
    }

    public static ModuleMap fromTag(CompoundTag tag, HolderLookup.Provider registries) {
        ModuleMap map = new ModuleMap();
        int size = tag.getInt("modules_size");
        for (int i=0; i<size; i++) {
            CompoundTag subTag = (CompoundTag) tag.get("module_%d".formatted(i));
            if (subTag == null) continue;
            ResourceLocation typeId = ResourceLocation.parse(subTag.getString("type"));
            Module module = Objects.requireNonNull(ModulesRegistry.MODULE_REGISTRY.get(typeId)).create(0, 0);
            module.loadData(subTag, registries);
            map.put(module.getName(), module);
        }
        return map;
    }

    public List<Map.Entry<String, Module>> asEntryList() {
        return this.entrySet().stream().toList();
    }

    public List<ModuleIOInfo> filterIOModules() {
        List<ModuleIOInfo> ret = new ArrayList<>();
        for (Map.Entry<String, Module> entry : this) {
            if ((entry.getValue() instanceof IInput ^ entry.getValue() instanceof IMultiInput) ||
                    (entry.getValue() instanceof IOutput ^ entry.getValue() instanceof IMultiOutput)) {
                ret.add(ModuleIOInfo.create(entry.getKey(), entry.getValue()));
            }
        }
        return ret;
    }

//    @Override
//    public Module get(Object key) {
//        if (!(key instanceof String str)) return null;
//        if (str.isEmpty()) return null;
//        for (ModuleIOInfo info : this.filterIOModules()) {
//            if (!ModuleIOInfo.hasMulti(info)) continue;
//            if (str.length() < info.name().length()) continue;
//            String sub = str.substring(0, info.name().length());
//            if (sub.equals(info.name())) {
//                return super.get(sub);
//            }
//        }
//        return super.get(key);
//    }

    public Module normalGet(Object key) {
        return super.get(key);
    }

//    @Override
//    public boolean containsKey(Object key) {
//        if (!(key instanceof String str)) return false;
//        if (str.isEmpty()) return false;
//        for (ModuleIOInfo info : this.filterIOModules()) {
//            if (info.type() == ModuleIOType.INPUT || info.type() == ModuleIOType.OUTPUT) continue;
//            if (str.length() < info.name().length()) continue;
//            String sub = str.substring(0, info.name().length());
//            if (sub.equals(info.name())) {
//                return super.containsKey(sub);
//            }
//        }
//        return super.containsKey(key);
//    }

    public boolean normalContainsKey(Object key) {
        return super.containsKey(key);
    }

    public Map<String, IModuleLuaObject> asGenericLuaMap() {
        Map<String, IModuleLuaObject> ret = new HashMap<>();
        for (Map.Entry<String, Module> entry : this) {
            if (entry.getValue() instanceof IModuleLuaObject luaObject)
                ret.put(entry.getKey(), luaObject);
        }
        return ret;
    }

    public Map<String, Pair<Module, IModuleLuaObject>> asGenericLuaPairMap() {
        Map<String, Pair<Module, IModuleLuaObject>> ret = new HashMap<>();
        for (Map.Entry<String, Module> entry : this) {
            if (entry.getValue() instanceof IModuleLuaObject luaObject)
                ret.put(entry.getKey(), new Pair<>(entry.getValue(), luaObject));
        }
        return ret;
    }

    @Override
    public @NonNull Iterator<Map.Entry<String, Module>> iterator() {
        return this.entrySet().iterator();
    }
}
