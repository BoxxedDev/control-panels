package moth.boxxed.panels.api.module;

import java.util.function.BiConsumer;

public interface IMultiInput {
    void getValues(BiConsumer<String, AnalogResult> consumer);

    @FunctionalInterface
    interface AnalogResult {
        int getAnalog();
    }
}
