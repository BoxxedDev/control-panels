package moth.boxxed.panels.api.module;

import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ModuleMap extends LinkedHashMap<String, Module> implements Iterable<Map.Entry<String, Module>> {
    public static ModuleMap empty() {
        return new ModuleMap();
    }

    public void rename(String oldName, String newName) {
        Module module = this.remove(oldName);
        this.put(newName, module);
    }

    public CompoundTag asTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("modules_size", this.size());
        for (int i=0; i<this.size(); i++) {
            Map.Entry<String, Module> moduleEntry = this.asEntryList().get(i);
            CompoundTag subTag = new CompoundTag();
            if (moduleEntry.getValue().saveData(subTag)) {
                subTag.putString("name", moduleEntry.getKey());
                tag.put("module_%d".formatted(i), subTag);
            }
        }
        return tag;
    }

    public static ModuleMap fromTag(CompoundTag tag) {
        ModuleMap map = new ModuleMap();
        int size = tag.getInt("modules_size");
        for (int i=0; i<size; i++) {
            CompoundTag subTag = (CompoundTag) tag.get("module_%d".formatted(i));
            if (subTag == null) continue;
            ResourceLocation typeId = ResourceLocation.parse(subTag.getString("type"));
            Module module = Objects.requireNonNull(ModulesRegistry.MODULE_REGISTRY.get(typeId)).create(0, 0);
            module.loadData(subTag);
            map.put(module.getName(), module);
        }
        return map;
    }

    public List<Map.Entry<String, Module>> asEntryList() {
        return this.entrySet().stream().toList();
    }

    public ModuleMap filterIOModules() {
        ModuleMap ret = new ModuleMap();
        for (Map.Entry<String, Module> entry : this) {
            if (entry.getValue() instanceof IInput || entry.getValue() instanceof IOutput)
                ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public Map<String, IModuleLuaObject> asGenericLuaMap() {
        Map<String, IModuleLuaObject> ret = new HashMap<>();
        for (Map.Entry<String, Module> entry : this) {
            if (entry.getValue() instanceof IModuleLuaObject luaObject)
                ret.put(entry.getKey(), luaObject);
        }
        return ret;
    }

    @Override
    public @NonNull Iterator<Map.Entry<String, Module>> iterator() {
        return this.entrySet().iterator();
    }
}
