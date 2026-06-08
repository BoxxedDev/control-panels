package moth.boxxed.panels.api.module;

import java.util.function.BiConsumer;

public interface IMultiOutput {
    void setValues(BiConsumer<String, AnalogRunnable> consumer);

    @FunctionalInterface
    interface AnalogRunnable {
        void setAnalog(int value);
    }
}