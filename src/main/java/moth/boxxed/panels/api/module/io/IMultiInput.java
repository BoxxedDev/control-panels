package moth.boxxed.panels.api.module.io;

import java.util.function.BiConsumer;

/**
 * Used to mark a module as multi input on the panel
 * e.g. joystick, steering wheel, etc. which then outputs a redstone signal or something of the sort
 * <br>
 * <br>
 * For now modules can ONLY be black or white, inputs or outputs
 */
public interface IMultiInput {
    /**
     *
     * @param consumer Accepts a name and an analog result:<br> () -> this.someinteger
     */
    void getValues(BiConsumer<String, AnalogResult> consumer);

    @FunctionalInterface
    interface AnalogResult {
        int getAnalog();
    }
}
