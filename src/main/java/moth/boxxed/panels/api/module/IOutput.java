package moth.boxxed.panels.api.module;

/**
 * Used to mark a module as an output on the panel
 * e.g. indicator light, display, etc. which then takes an input redstone signal or something similar
 * <br>
 * <br>
 * For now modules can ONLY be black or white, inputs or outputs
 */
public interface IOutput {
    /**
     * Signal input which is a redstone signal from 0-15 which can be mapped to whatever other value you want
     * @param signal
     */
    void setAnalog(int signal);
}