package moth.boxxed.panels.api.module.io;

/**
 * Used to mark a moduleName as an input on the panel
 * e.g. switch, button, etc. which then outputs a redstone signal or something of the sort
 * <br>
 * <br>
 * For now modules can ONLY be black or white, inputs or outputs
 */
public interface IInput {
    /**
     * @return Output redstone signal
     */
    int getAnalog();
}