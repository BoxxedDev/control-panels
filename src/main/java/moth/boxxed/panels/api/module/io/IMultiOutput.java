package moth.boxxed.panels.api.module.io;

import java.util.function.BiConsumer;

/**
 * Used to mark a moduleName as multi output on the panel
 * e.g. screen, levels display, etc. which then takes an input redstone signal or something similar
 * <br>
 * <br>
 * For now modules can ONLY be black or white, inputs or outputs
 */
public interface IMultiOutput {
    void setValues(BiConsumer<String, AnalogRunnable> consumer);

    @FunctionalInterface
    interface AnalogRunnable {
        void setAnalog(int value);
    }
}