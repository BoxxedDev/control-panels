package moth.boxxed.panels.api.module;

import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModuleMap extends HashMap<String, Module> implements Iterable<Map.Entry<String, Module>> {
    public static ModuleMap empty() {
        return new ModuleMap();
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
