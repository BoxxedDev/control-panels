package moth.boxxed.panels.api.module;

import moth.boxxed.panels.api.module.io.*;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleMap extends LinkedHashMap<String, Module> implements Iterable<Map.Entry<String, Module>> {
    public static ModuleMap empty() {
        return new ModuleMap();
    }

    public void rename(String oldName, String newName) {
        Module module = this.remove(oldName);
        this.put(newName, module);
    }

    public ListTag asTag(HolderLookup.Provider registries) {
        ListTag tag = new ListTag(this.size());
        for (Module module : this.values()) {
            CompoundTag subTag = new CompoundTag();
            if (module.saveData(subTag, registries)) {
                tag.add(subTag);
            }
        }
        return tag;
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

    @Deprecated(since = "2.2")
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

    @Deprecated(since = "2.2")
    public boolean normalContainsKey(Object key) {
        return super.containsKey(key);
    }

    @Deprecated(since = "2.2")
    public Map<String, IModuleLuaObject> asGenericLuaMap() {
        Map<String, IModuleLuaObject> ret = new HashMap<>();
        for (Map.Entry<String, Module> entry : this) {
            if (entry.getValue() instanceof IModuleLuaObject luaObject)
                ret.put(entry.getKey(), luaObject);
        }
        return ret;
    }

    @Deprecated(since = "2.2")
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
