package moth.boxxed.panels.api.module.config;

import java.util.LinkedHashSet;

public class ModuleConfig {
    private final LinkedHashSet<ModuleConfigValue<?, ?>> configValues;

    protected ModuleConfig(LinkedHashSet<ModuleConfigValue<?, ?>> configValues) {
        this.configValues = configValues;
    }

    public LinkedHashSet<ModuleConfigValue<?,?>> getValues() {
        return new LinkedHashSet<>(this.configValues);
    }

    public static class Builder {
        protected LinkedHashSet<ModuleConfigValue<?,?>> configValues = new LinkedHashSet<>();

        public <T, R extends ModuleConfigValue<T, R>> void add(R configValue) {
            this.configValues.add(configValue);
        }

        public ModuleConfig build() {
            return new ModuleConfig(this.configValues);
        }
    }
}
