package moth.boxxed.panels.api.module;

import org.jspecify.annotations.NonNull;

import java.util.*;

public class ModuleMap extends HashMap<String, Module> implements Iterable<Map.Entry<String, Module>> {
    public static ModuleMap Empty() {
        return new ModuleMap();
    }

    public void rename(Map.Entry<String, Module> entry, String newName) {
        this.rename(entry.getKey(), newName);
    }

    public void rename(String oldName, String newName) {
        Module module = this.remove(oldName);
        this.put(newName, module);
    }

    @Override
    public @NonNull Iterator<Entry<String, Module>> iterator() {
        return this.entrySet().iterator();
    }
}
